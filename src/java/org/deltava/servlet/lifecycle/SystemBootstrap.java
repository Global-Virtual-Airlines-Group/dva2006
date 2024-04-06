// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.ETOPSHelper;
import org.deltava.beans.navdata.Airspace;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.stats.AirlineTotals;

import org.deltava.dao.*;
import org.deltava.discord.Bot;
import org.deltava.mail.MailerDaemon;
import org.deltava.security.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.jmx.*;
import org.deltava.util.ipc.IPCDaemon;
import org.deltava.util.system.*;

import org.gvagroup.common.*;
import org.gvagroup.jdbc.*;
import org.gvagroup.tomcat.SharedWorker;

import com.newrelic.api.agent.NewRelic;

/**
 * The System bootstrap loader, that fires when the servlet container is started or stopped.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class SystemBootstrap implements ServletContextListener, Thread.UncaughtExceptionHandler {

	private static final Logger log = LogManager.getLogger(SystemBootstrap.class);

	private ConnectionPool _jdbcPool;
	private final Map<Thread, Runnable> _daemons = new HashMap<Thread, Runnable>();

	@Override
	public void contextInitialized(ServletContextEvent e) {
		TaskTimer tt = new TaskTimer();
		e.getServletContext().setAttribute("startedOn", java.time.Instant.now());

		// Initialize system data
		SystemData.init();
		String code = SystemData.get("airline.code");
		log.info("Starting {}", code);
		SharedData.addApp(code);
		
		// Load Secrets
		if (SystemData.has("security.secrets")) {
			try {
				SecretManager sm = new PropertiesSecretManager(SystemData.get("security.secrets"));
				sm.load();
				sm.getKeys().forEach(k -> SystemData.add(k, sm.get(k)));
				log.info("Loaded {} secrets from {}", Integer.valueOf(sm.size()), SystemData.get("security.secrets"));
			} catch (IOException ie) {
				log.atError().withThrowable(ie).log("Error loading secrets - {}", ie.getMessage());
			}
		}
		
		// Set start date
		AirlineTotals.BIRTHDATE = StringUtils.parseInstant(SystemData.get("airline.birthdate"), "MM/dd/yyyy");
		
		// Initialize caches
		try (InputStream is = ConfigLoader.getStream("/etc/cacheInfo.xml")) {
			CacheLoader.load(is);
		} catch(IOException ie) {
			log.warn("Cannot configure caches from code - {}", ie.getMessage());
		}
		
		// Init Redis
		RedisUtils.init(SystemData.get("redis.addr"), SystemData.getInt("redis.port", 6379), SystemData.getInt("redis.db", 0), code);
		
		// Load caches into JMX
		JMXCacheManager cm = new JMXCacheManager(code);
		JMXUtils.register("org.gvagroup:type=CacheManager,name=" + code, cm);
		SharedWorker.register(new JMXRefreshTask(cm, 60000));

		// Initialize the connection pool
		log.info("Starting JDBC connection pool");
		_jdbcPool = new ConnectionPool(SystemData.getInt("jdbc.pool_max_size", 2), code);
		_jdbcPool.setProperties((Map<?, ?>) SystemData.getObject("jdbc.connectProperties"));
		_jdbcPool.setCredentials(SystemData.get("jdbc.user"), SystemData.get("jdbc.pwd"));
		_jdbcPool.setURL(SystemData.get("jdbc.url"));
		_jdbcPool.setMaxRequests(SystemData.getInt("jdbc.max_reqs", 0));
		_jdbcPool.setLogStack(SystemData.getBoolean("jdbc.log_stack"));

		// Attempt to load the driver and connect
		try {
			_jdbcPool.setDriver(SystemData.get("jdbc.driver"));
			_jdbcPool.setSocket(SystemData.get("jdbc.socket"));
			_jdbcPool.connect(SystemData.getInt("jdbc.pool_size"));
			JMXConnectionPool jmxpool = new JMXConnectionPool(code, _jdbcPool);
			JMXUtils.register("org.gvagroup:type=JDBCPool,name=" + code, jmxpool);
			SharedWorker.register(new JMXRefreshTask(jmxpool, 60000));
		} catch (ClassNotFoundException cnfe) {
			log.error("Cannot load JDBC driver class - {}", SystemData.get("jdbc.Driver"));
		} catch (ConnectionPoolException cpe) {
			Throwable t = cpe.getCause();
			log.atError().withThrowable(t).log("Error connecting to JDBC data source - {}", t.getMessage());
		}

		// Save the connection pool in the SystemData
		SystemData.add(SystemData.JDBC_POOL, _jdbcPool);
		SharedData.addData(SharedData.JDBC_POOL + code, _jdbcPool);
		
		// Get and load the authenticator
		String authClass = SystemData.get("security.auth");
		try {
			Class<?> c = Class.forName(authClass);
			Authenticator auth = (Authenticator) c.getDeclaredConstructor().newInstance();
			log.debug("Loaded class {}", authClass);

			// Initialize and store in the servlet context
			auth.init(Authenticator.DEFAULT_PROPS_FILE);
			SystemData.add(SystemData.AUTHENTICATOR, auth);
		} catch (ClassNotFoundException cnfe) {
			log.error("Cannot find authenticator class {}", authClass);
		} catch (SecurityException se) {
			log.error("Error initializing authenticator - {}", se.getMessage());
		} catch (Exception ex) {
			log.error("Error starting authenticator - {} - {}", ex.getClass().getName(), ex.getMessage());
		}

		// Start the Task Manager
		TaskScheduler taskSched = null;
		try {
			taskSched = new TaskScheduler(TaskFactory.load(SystemData.get("config.tasks")));
			SystemData.add(SystemData.TASK_POOL, taskSched);
			spawnDaemon(taskSched);
		} catch (IOException ie) {
			log.atError().withThrowable(ie).log("Cannot load scheduled tasks - {}", ie.getMessage());
		}

		// Load data from the database
		Connection c = null;
		try {
			// Get JDBC system connection
			c = _jdbcPool.getConnection();

			// Load time zones
			log.info("Loading Time Zones");
			GetTimeZone dao = new GetTimeZone(c);
			dao.initAll();
			log.info("Loaded Time Zones");
			
			// Load country codes
			log.info("Loading Country codes");
			GetCountry cdao = new GetCountry(c);
			log.info("Loaded {} Country codes", Integer.valueOf(cdao.initAll()));

			// Load Database information
			log.info("Loading Cross-Application data");
			GetUserData uddao = new GetUserData(c);
			SystemData.add("apps", uddao.getAirlines(true));

			// Load active airlines
			log.info("Loading Airline Codes");
			GetAirline dao1 = new GetAirline(c);
			SystemData.add("airlines", dao1.getAll());

			// Load airports
			log.info("Loading Airports");
			GetAirport dao2 = new GetAirport(c);
			Map<String, Airport> airports = dao2.getAll(); 
			SystemData.add("airports", airports);
			ETOPSHelper.init(airports.values());
			log.info("Initialized ETOPS helper");
			
			// Load prohibited airspace
			log.info("Loading restricted Airspace");
			GetAirspace asdao = new GetAirspace(c);
			Airspace.init(asdao.getRestricted());

			// Load last execution date/times for Scheduled Tasks
			if (taskSched != null) {
				log.info("Loading Scheduled Task execution data");
				GetSystemData sysdao = new GetSystemData(c);
				Map<String, TaskLastRun> taskInfo = sysdao.getTaskExecution();
				taskInfo.values().forEach(taskSched::setLastRunTime);
			}
			
			// Load User Pool max values
			String prefix = code.toLowerCase();
			GetMetadata mddao = new GetMetadata(c);
			UserPool.init(StringUtils.parse(mddao.get(prefix + ".users.max.count"), 0), StringUtils.parseInstant(mddao.get(prefix + ".users.max.date"), "MM/dd/yyyy HH:mm"));
		} catch (Exception ex) {
			log.atError().withThrowable(ex).log("Error retrieving data - {}", ex.getMessage());
		} finally {
			_jdbcPool.release(c);
		}
		
		// Load economy data
		if (!StringUtils.isEmpty(SystemData.get("econ.targetLoad"))) {
			EconomyInfo econInfo = new EconomyInfo(SystemData.getDouble("econ.targetLoad", 0.8d), SystemData.getDouble("econ.targetAmplitude", 0.125));
			econInfo.setMinimumLoad(SystemData.getDouble("econ.minimumLoad", 0.25));
			econInfo.setStartDate(AirlineTotals.BIRTHDATE);
			econInfo.setCycleLength(SystemData.getInt("econ.yearlyCycleLength", 365));
			SystemData.add(SystemData.ECON_DATA, econInfo);
			SharedData.addData(SharedData.ECON_DATA + code, econInfo);
			log.info("Loaded Economic parameters");
		}
		
		// Load elite program
		if (SystemData.getBoolean("econ.elite.enabled")) {
			EliteProgram ep = new EliteProgram(SystemData.get("econ.elite.name"));
			ep.setUnits(SystemData.get("econ.elite.distance"), SystemData.get("econ.elite.points"));
			SharedData.addData(SharedData.ELITE_INFO + code, ep);
			log.info("Added {} program info", ep.getName());
		}
		
		// Load discord bot
		if (SystemData.getBoolean("discord.bot")) {
			try {
				Bot.init();
				log.info("Loaded Discord server bot");
			} catch (Exception ex) {
				log.atError().withThrowable(ex).log("Error initializing Discord bot - {}", ex.getMessage());
			}
		}
		
		// Start the mailer/IPC daemons
		spawnDaemon(new MailerDaemon());
		spawnDaemon(new IPCDaemon());
		log.warn("Started {} in {}ms", code, Long.valueOf(tt.stop()));
	}

	@Override
	public void contextDestroyed(ServletContextEvent e) {
		String code = SystemData.get("airline.code");
		log.warn("Shutting Down {}", code);

		// Shut down the extra threads
		Collection<Thread> dt = new ArrayList<Thread>(_daemons.keySet());
		_daemons.clear();
		ThreadUtils.kill(dt, 500);
		
		// Clean up shared worker and JMX
		JMXUtils.clear();
		SharedWorker.clear(Thread.currentThread().getContextClassLoader());
		
		// Shut down Redis and JDBC connection pools
		try {
			Thread.sleep(750);
		} catch (InterruptedException ie) {
			log.warn("Interrupted waiting for servlets to clean up");
		} finally {
			RedisUtils.shutdown();
			_jdbcPool.close();
		}
		
		if (SystemData.getBoolean("discord.bot"))
			Bot.disconnect();
		
		// Clear shared data
		SharedData.purge(code);
		log.error("Shut down {}", code);
	}

	/*
	 * Helper method to spawn a system daemon.
	 */
	private void spawnDaemon(Runnable sd) {
		Thread dt = Thread.ofVirtual().name(sd.toString()).unstarted(sd);
		dt.setUncaughtExceptionHandler(this);
		_daemons.put(dt, sd);
		dt.start();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Runnable sd = _daemons.get(t);
		if (sd == null) {
			log.error("Unknown daemon thread - {}", t.getName());
			return;
		}

		// Restart the daemon
		log.atError().withThrowable(e).log("Restarting {}", sd);
		NewRelic.noticeError(e, false);
		synchronized (_daemons) {
			_daemons.remove(t);
			spawnDaemon(sd);
		}
	}
}
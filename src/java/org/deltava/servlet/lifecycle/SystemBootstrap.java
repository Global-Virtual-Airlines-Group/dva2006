// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;

import org.apache.log4j.Logger;

import org.deltava.beans.econ.EconomyInfo;
import org.deltava.beans.flight.ETOPSHelper;
import org.deltava.beans.navdata.Airspace;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.stats.AirlineTotals;

import org.deltava.dao.*;

import org.deltava.mail.MailerDaemon;
import org.deltava.security.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.ipc.IPCDaemon;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;
import org.gvagroup.jdbc.*;

/**
 * The System bootstrap loader, that fires when the servlet container is started or stopped.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SystemBootstrap implements ServletContextListener, Thread.UncaughtExceptionHandler {

	private static final Logger log = Logger.getLogger(SystemBootstrap.class);

	private ConnectionPool _jdbcPool;
	private final ThreadGroup _daemonGroup = new ThreadGroup("System Daemons");
	private final Map<Thread, Runnable> _daemons = new HashMap<Thread, Runnable>();

	/**
	 * Initialize the servlet context. This method will initialize the SystemData singleton, create the JDBC connection
	 * pool and load "long-lived" bean collections like the Airlines and Airports.
	 * @param e the ServletContext lifecycle event
	 */
	@Override
	public void contextInitialized(ServletContextEvent e) {
		e.getServletContext().setAttribute("startedOn", java.time.Instant.now());

		// Initialize system data
		SystemData.init();
		String code = SystemData.get("airline.code");
		log.info("Starting " + code);
		SharedData.addApp(code);
		
		// Set start date
		AirlineTotals.BIRTHDATE = StringUtils.parseInstant(SystemData.get("airline.birthdate"), "MM/dd/yyyy");
		
		// Initialize caches
		try (InputStream is = ConfigLoader.getStream("/etc/cacheInfo.xml")) {
			CacheLoader.load(is);
		} catch(IOException ie) {
			log.warn("Cannot configure caches from code");
		}

		// Initialize the connection pool
		log.info("Starting JDBC connection pool");
		_jdbcPool = new ConnectionPool(SystemData.getInt("jdbc.pool_max_size", 2), SystemData.get("airline.code"));
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
		} catch (ClassNotFoundException cnfe) {
			log.error("Cannot load JDBC driver class - " + SystemData.get("jdbc.Driver"));
		} catch (ConnectionPoolException cpe) {
			Throwable t = cpe.getCause();
			log.error("Error connecting to JDBC data source - " + t.getMessage(), t);
		}

		// Save the connection pool in the SystemData
		SystemData.add(SystemData.JDBC_POOL, _jdbcPool);
		SharedData.addData(SharedData.JDBC_POOL + SystemData.get("airline.code"), _jdbcPool);
		
		// Init Redis
		RedisUtils.init(SystemData.get("redis.addr"), SystemData.getInt("redis.port", 6379), SystemData.getInt("redis.db", 0), code);

		// Get and load the authenticator
		String authClass = SystemData.get("security.auth");
		try {
			Class<?> c = Class.forName(authClass);
			Authenticator auth = (Authenticator) c.getDeclaredConstructor().newInstance();
			log.debug("Loaded class " + authClass);

			// Initialize and store in the servlet context
			auth.init(Authenticator.DEFAULT_PROPS_FILE);
			SystemData.add(SystemData.AUTHENTICATOR, auth);
		} catch (ClassNotFoundException cnfe) {
			log.error("Cannot find authenticator class " + authClass);
		} catch (SecurityException se) {
			log.error("Error initializing authenticator - " + se.getMessage());
		} catch (Exception ex) {
			log.error("Error starting authenticator - " + ex.getClass().getName() + " - " + ex.getMessage());
		}

		// Start the Task Manager
		TaskScheduler taskSched = null;
		try {
			taskSched = new TaskScheduler(TaskFactory.load(SystemData.get("config.tasks")));
			SystemData.add(SystemData.TASK_POOL, taskSched);
			spawnDaemon(taskSched);
		} catch (IOException ie) {
			log.error("Cannot load scheduled tasks - " + ie.getMessage(), ie);
		}

		// Load data from the database
		Connection c = null;
		try {
			// Get JDBC system connection
			c = _jdbcPool.getConnection();

			// Load time zones
			log.info("Loading Time Zones");
			GetTimeZone dao = new GetTimeZone(c);
			log.info("Loaded " + dao.initAll() + " Time Zones");
			
			// Load country codes
			log.info("Loading Country codes");
			GetCountry cdao = new GetCountry(c);
			log.info("Loaded " + cdao.initAll() + " Country codes");

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
				for (TaskLastRun tlr : taskInfo.values())
					taskSched.setLastRunTime(tlr);
			}
			
			// Load User Pool max values
			String prefix = SystemData.get("airline.code").toLowerCase();
			GetMetadata mddao = new GetMetadata(c);
			UserPool.init(StringUtils.parse(mddao.get(prefix + ".users.max.count"), 0), StringUtils.parseInstant(mddao.get(prefix + ".users.max.date"), "MM/dd/yyyy HH:mm"));
		} catch (Exception ex) {
			log.error("Error retrieving data - " + ex.getMessage(), ex);
		} finally {
			_jdbcPool.release(c);
		}
		
		// Load economy data
		if (!StringUtils.isEmpty(SystemData.get("econ.targetLoad"))) {
			EconomyInfo econInfo = new EconomyInfo(SystemData.getDouble("econ.targetLoad", 0.8d), SystemData.getDouble("econ.targetAmplitude", 0.125));
			econInfo.setMinimumLoad(SystemData.getDouble("econ.minimumLoad", 0.25));
			econInfo.setStartDate(AirlineTotals.BIRTHDATE);
			econInfo.setHourlyFactor(SystemData.getDouble("econ.hourlyFactor", 0.0));
			econInfo.setYearlyCycleLength(SystemData.getInt("econ.yearlyCycleLength", 365));
			econInfo.setHourlyCycleLength(SystemData.getInt("econ.hourlyCycleLength", 24));
			SystemData.add(SystemData.ECON_DATA, econInfo);
			SharedData.addData(SharedData.ECON_DATA + SystemData.get("airline.code"), econInfo);
			log.info("Loaded Economic parameters");
		}
		
		// Start the mailer/IPC daemons
		spawnDaemon(new MailerDaemon());
		spawnDaemon(new IPCDaemon());
	}

	/**
	 * Shut down resources used by the servlet context.
	 * @param e the ServletContext lifecycle event
	 */
	@Override
	public void contextDestroyed(ServletContextEvent e) {
		String code = SystemData.get("airline.code");
		log.warn("Shutting Down " + code);

		// Shut down the extra threads
		_daemons.clear();
		_daemonGroup.interrupt();
		
		// Shut down Redis and JDBC connection pools
		RedisUtils.shutdown();
		_jdbcPool.close();
		
		// Clear shared data
		SharedData.purge(code);
		log.error("Shut down " + code);
	}

	/*
	 * Helper method to spawn a system daemon.
	 */
	private void spawnDaemon(Runnable sd) {
		Thread dt = new Thread(_daemonGroup, sd, sd.toString());
		dt.setDaemon(true);
		dt.setUncaughtExceptionHandler(this);
		_daemons.put(dt, sd);
		dt.start();
	}

	/**
	 * Uncaught system daemon thread exception handler.
	 * @param t the daemon thred
	 * @param e the uncaught exception
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Runnable sd = _daemons.get(t);
		if (sd == null) {
			log.error("Unknown daemon thread - " + t.getName());
			return;
		}

		// Restart the daemon
		log.error("Restarting " + sd, e);
		synchronized (_daemons) {
			_daemons.remove(t);
			spawnDaemon(sd);
		}
	}
}
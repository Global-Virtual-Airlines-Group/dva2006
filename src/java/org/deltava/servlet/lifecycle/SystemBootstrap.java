// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;

import org.apache.log4j.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.jdbc.*;

import org.deltava.mail.MailerDaemon;

import org.deltava.security.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.ipc.IPCDaemon;
import org.deltava.util.servinfo.ServInfoLoader;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.SharedData;

/**
 * The System bootstrap loader, that fires when the servlet container is started or stopped.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class SystemBootstrap implements ServletContextListener, Thread.UncaughtExceptionHandler {

	private static Logger log;

	private ConnectionPool _jdbcPool;
	private final ThreadGroup _daemonGroup = new ThreadGroup("System Daemons");
	private final Map<Thread, Runnable> _daemons = new HashMap<Thread, Runnable>();

	/**
	 * Initialize the System bootstrap loader, and configure log4j.
	 */
	public SystemBootstrap() {
		super();
		PropertyConfigurator.configure(getClass().getResource("/etc/log4j.properties"));
		SystemBootstrap.log = Logger.getLogger(SystemBootstrap.class);
		log.info("Initialized log4j");

		// Force headless AWT operation
		System.setProperty("java.awt.headless", "true");
	}

	/**
	 * Initialize the servlet context. This method will initialize the SystemData singleton, create the JDBC connection
	 * pool and load "long-lived" bean collections like the Airlines and Airports.
	 * @param e the ServletContext lifecycle event
	 */
	public void contextInitialized(ServletContextEvent e) {
		e.getServletContext().setAttribute("startedOn", new java.util.Date());
		_daemonGroup.setDaemon(true);

		// Initialize system data
		SystemData.init();
		SharedData.addApp(SystemData.get("airline.code"));

		// Load the profanity filter
		try {
			InputStream is = ConfigLoader.getStream("/etc/profanity.txt");
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is));

			// Load the content
			Collection<String> words = new LinkedHashSet<String>();
			while (lr.ready())
				words.add(lr.readLine());

			lr.close();

			// Init the profanity filter
			log.info("Initializing Content Filter");
			ProfanityFilter.init(words);
		} catch (IOException ie) {
			log.warn("Cannot load Profanity Filter - " + ie.getMessage());
		}
		
		// Initialize the connection pool
		log.info("Starting JDBC connection pool");
		_jdbcPool = new ConnectionPool(SystemData.getInt("jdbc.pool_max_size"));
		_jdbcPool.setProperties((Map) SystemData.getObject("jdbc.connectProperties"));
		_jdbcPool.setCredentials(SystemData.get("jdbc.user"), SystemData.get("jdbc.pwd"));
		_jdbcPool.setProperty("url", SystemData.get("jdbc.url"));
		_jdbcPool.setMaxRequests(SystemData.getInt("jdbc.max_reqs", 0));
		_jdbcPool.setLogStack(SystemData.getBoolean("jdbc.log_stack"));

		// Attempt to load the driver and connect
		try {
			_jdbcPool.setDriver(SystemData.get("jdbc.driver"));
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

		// Get and load the authenticator
		String authClass = SystemData.get("security.auth");
		try {
			Class c = Class.forName(authClass);
			log.debug("Loaded class " + authClass);
			Authenticator auth = (Authenticator) c.newInstance();

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
			c = _jdbcPool.getConnection(true);

			// Load time zones
			log.info("Loading Time Zones");
			GetTimeZone dao = new GetTimeZone(c);
			dao.initAll();

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
			SystemData.add("airports", dao2.getAll());

			// Load last execution date/times for Scheduled Tasks
			if (taskSched != null) {
				log.info("Loading Scheduled Task execution data");
				GetSystemData sysdao = new GetSystemData(c);
				Map<String, TaskLastRun> taskInfo = sysdao.getTaskExecution();
				for (Iterator<TaskLastRun> i = taskInfo.values().iterator(); i.hasNext();) {
					TaskLastRun tlr = i.next();
					taskSched.setLastRunTime(tlr);
				}
			}
			
			// Load User Pool max values
			InputStream is = ConfigLoader.getStream("/etc/maxUsers.properties");
			if (is != null) {
				GetProperties pdao = new GetProperties(is);
				Properties p = pdao.read();
				UserPool.init(StringUtils.parse(p.getProperty("users"), 0), StringUtils.parseDate(p.getProperty("date"), "MM/dd/yyyy HH:mm"));
			}

			// Load TS2 server info if enabled
			if (SystemData.getBoolean("airline.voice.ts2.enabled") && SystemData.getBoolean("acars.enabled")) {
				SetTS2Data ts2wdao = new SetTS2Data(c);
				int flagsCleared = ts2wdao.clearActiveFlags();
				if (flagsCleared > 0)
					log.warn("Reset " + flagsCleared + " TeamSpeak 2 client activity flags");
			}
		} catch (Exception ex) {
			log.error("Error retrieving data - " + ex.getMessage(), ex);
		} finally {
			_jdbcPool.release(c);
		}

		// Get online network information
		List networks = (List) SystemData.getObject("online.networks");
		for (Iterator i = networks.iterator(); i.hasNext();) {
			String network = (String) i.next();
			log.info("Loading " + network + " data");

			// Load the data
			ServInfoLoader loader = new ServInfoLoader(network);
			Thread t = new Thread(loader, network + " ServInfo Loader");
			t.setDaemon(true);
			t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
			ServInfoLoader.addLoader(network, t);
		}

		// Start the mailer/IPC daemons
		spawnDaemon(new MailerDaemon());
		spawnDaemon(new IPCDaemon());
	}

	/**
	 * Shut down resources used by the servlet context.
	 * @param e the ServletContext lifecycle event
	 */
	public void contextDestroyed(ServletContextEvent e) {
		log.warn("Shutting Down");

		// Shut down the extra threads
		_daemonGroup.interrupt();
		
		// If ACARS is enabled, then clean out the active flags
		if (SystemData.getBoolean("airline.voice.ts2.enabled") && SystemData.getBoolean("acars.enabled")) {
			log.info("Resetting TeamSpeak 2 client activity flags");

			Connection c = null;
			try {
				c = _jdbcPool.getConnection(true);

				SetTS2Data ts2wdao = new SetTS2Data(c);
				ts2wdao.clearActiveFlags();
			} catch (DAOException de) {
				log.error(de.getMessage(), de);
			} finally {
				_jdbcPool.release(c);
			}
		}

		// Shut down the JDBC connection pool
		ThreadUtils.sleep(2000);
		_jdbcPool.close();

		// Deregister JDBC divers
		SharedData.purge(SystemData.get("airline.code"));
		for (Enumeration<Driver> en = DriverManager.getDrivers(); en.hasMoreElements();) {
			Driver driver = en.nextElement();
			if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
				try {
					DriverManager.deregisterDriver(driver);
					log.info("Deregistered JDBC driver " + driver.getClass().getName());
				} catch (Exception ex) {
					log.error("Error dregistering " + driver.getClass(), ex);
				}
			}
		}

		// Close the Log4J manager
		LogManager.shutdown();
	}

	/**
	 * Helper method to spawn a system daemon.
	 * @param sd the daemon to spawn
	 * @param isLower TRUE if the thread should run with slightly lower priority, otherwise FALSE
	 */
	private void spawnDaemon(Runnable sd) {
		Thread dt = new Thread(_daemonGroup, sd, sd.toString());
		dt.setUncaughtExceptionHandler(this);
		_daemons.put(dt, sd);
		dt.start();
	}

	/**
	 * Uncaught system daemon thread exception handler.
	 * @param t the daemon thred
	 * @param e the uncaught exception
	 */
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
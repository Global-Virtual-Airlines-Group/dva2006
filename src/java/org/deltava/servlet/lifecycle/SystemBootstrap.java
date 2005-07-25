package org.deltava.servlet.lifecycle;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import java.io.IOException;

import org.apache.log4j.*;

import org.deltava.dao.*;
import org.deltava.jdbc.*;
import org.deltava.security.*;
import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * The System bootstrap loader, that fires when the servlet container is started or stopped.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SystemBootstrap implements ServletContextListener {

	private static Logger log;
	private ConnectionPool _jdbcPool;
	private TaskScheduler _taskSched;

	private Runnable _acarsServer;
	private Thread _acarsThread;

	/**
	 * Initialize the System bootstrap loader, and configure log4j.
	 */
	public SystemBootstrap() {
		super();
		PropertyConfigurator.configure(getClass().getResource("/etc/log4j.properties"));
		SystemBootstrap.log = Logger.getLogger(SystemBootstrap.class);
		
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

		// Initialize system data
		SystemData.init();

		// Get and load the authenticator
		String authClass = SystemData.get("security.auth");
		Authenticator auth = null;
		try {
			Class c = Class.forName(authClass);
			log.debug("Loaded class " + authClass);
			auth = (Authenticator) c.newInstance();

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

		// Initialize the connection pool
		log.info("Starting JDBC connection pool");
		_jdbcPool = new ConnectionPool(SystemData.getInt("jdbc.pool_max_size"));
		_jdbcPool.setProperties((Map) SystemData.getObject("jdbc.connectProperties"));
		_jdbcPool.setCredentials(SystemData.get("jdbc.user"), SystemData.get("jdbc.pwd"));
		_jdbcPool.setProperty("url", SystemData.get("jdbc.url"));

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

		// Load data from the database
		Connection c = null;
		try {
			// Get JDBC system connection
			c = _jdbcPool.getSystemConnection();

			// Load time zones
			log.info("Loading Time Zones");
			GetTimeZone dao = new GetTimeZone(c);
			dao.initAll();

			// Load active airlines
			log.info("Loading active Airlines");
			GetAirline dao1 = new GetAirline(c);
			SystemData.add("airlines", dao1.getActive());

			// Load airports
			log.info("Loading Airports");
			GetAirport dao2 = new GetAirport(c);
			SystemData.add("airports", dao2.getAll());
		} catch (Exception ex) {
			log.error("Error retrieving data - " + ex.getMessage());
		} finally {
			_jdbcPool.release(c);
		}

		// Start the ACARS server
		if (SystemData.getBoolean("acars.enabled")) {
			try {
				Class acarsSrvC = Class.forName(SystemData.get("acars.daemon"));
				_acarsServer = (Runnable) acarsSrvC.newInstance();
			} catch (ClassNotFoundException cnfe) {
				log.error("Cannot find ACARS Daemon " + SystemData.get("acars.daemon"));
			} catch (Exception ex) {
				log.error("Error Starting ACARS Daemon", ex);
			}

			// Start the server
			_acarsThread = new Thread(_acarsServer, "ACARS Daemon");
			_acarsThread.setDaemon(true);
			_acarsThread.start();
		}
		
		// Start the Task Manager
		try {
			_taskSched = new TaskScheduler(TaskFactory.load(SystemData.get("config.tasks")));
			SystemData.add(SystemData.TASK_POOL, _taskSched);
			_taskSched.start();
		} catch (IOException ie) {
			log.error("Error loading Task Scheduler configuration - " + ie.getMessage(), ie);
		}
	}

	/**
	 * Shut down resources used by the servlet context.
	 * @param e the ServletContext lifecycle event
	 */
	public void contextDestroyed(ServletContextEvent e) {

		// Shut down the ACARS server
		ThreadUtils.kill(_acarsThread, 500);
		
		// Shut down the task scheduler
		ThreadUtils.kill(_taskSched, 500);

		// Shut down and remove the JDBC connection pool
		e.getServletContext().removeAttribute("jdbcConnectionPool");
		if (_jdbcPool != null)
			_jdbcPool.close();

		// Close the Log4J manager
		LogManager.shutdown();
	}
}
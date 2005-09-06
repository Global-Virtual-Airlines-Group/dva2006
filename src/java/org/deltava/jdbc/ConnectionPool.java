package org.deltava.jdbc;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.util.ThreadUtils;

/**
 * A class that implements a user-configurable JDBC Connection Pool.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see ConnectionPoolEntry
 * @see ConnectionMonitor
 */

public class ConnectionPool implements Recycler {

	private static final Logger log = Logger.getLogger(ConnectionPool.class);

	// The maximum amount of time a connection can be reserved before we consider
	// it to be stale and return it anyways
	static final int MAX_USE_TIME = 58000;

	private int _poolMaxSize = 1;
	private int _lastID;

	private ConnectionMonitor _monitor;

	private List _cons;
	private List _sysCons;

	private Properties _props;
	private boolean _autoCommit = true;

	/**
	 * Creates a new JDBC connection pool.
	 * @param maxSize the maximum size of the connection pool
	 */
	public ConnectionPool(int maxSize) {
		super();
		DriverManager.setLoginTimeout(5);
		_props = new Properties();
		_poolMaxSize = maxSize;
		_monitor = new ConnectionMonitor();
	}

	private void checkConnected() throws IllegalStateException {
		if (_cons == null)
			throw new IllegalStateException("Pool not connected");
	}

	/**
	 * Returns the JDBC URL of this connection pool.
	 * @return the JDBC URL to connect to
	 */
	public String getURL() {
		return _props.getProperty("url", "");
	}

	/**
	 * Returns the current size of the connection pool.
	 * @return the number of open connections, or -1 if not connected
	 */
	public int getSize() {
		return (_cons == null) ? -1 : _cons.size();
	}

	/**
	 * Returns the maximum size of the connection pool.
	 * @return the maximum number of connections that can be opened
	 */
	public int getMaxSize() {
		return _poolMaxSize;
	}

	/**
	 * Updates the database auto-commit setting.
	 * @param commit TRUE if statements are committed automatically, otherwise FALSE
	 */
	public void setAutoCommit(boolean commit) {
		_autoCommit = commit;
	}

	/**
	 * Sets the credentials used to connect to the JDBC data source.
	 * @param user the User ID
	 * @param pwd the password
	 */
	public void setCredentials(String user, String pwd) {
		_props.setProperty("user", user);
		_props.setProperty("password", pwd);
	}

	/**
	 * Sets a JDBC connection property.
	 * @param propertyName the property name
	 * @param propertyValue the property value
	 */
	public void setProperty(String propertyName, String propertyValue) {
		_props.setProperty(propertyName, propertyValue);
	}

	/**
	 * Sets multiple JDBC connection properties at once.
	 * @param props the properties to set
	 */
	public void setProperties(Map props) {
		_props.putAll(props);
	}

	/**
	 * Sets the JDBC Driver class name.
	 * @param driverClassName the fully-qualified class name of the JDBC driver
	 * @throws ClassNotFoundException if the class cannot be loaded
	 */
	public void setDriver(String driverClassName) throws ClassNotFoundException {
		Class.forName(driverClassName);
	}

	/**
	 * Adds a new connection to the connection pool.
	 * @return the new connection pool entry
	 * @throws SQLException if a JDBC error occurs connecting to the data source.
	 */
	protected ConnectionPoolEntry createConnection(boolean isSystem) throws SQLException {
		int id = ++_lastID;
		log.info("Connecting to " + getURL() + " as user " + _props.getProperty("user", "") + " ID #" + ((isSystem) ? "SYS" : "") + id);
		ConnectionPoolEntry entry = new ConnectionPoolEntry(id, getURL(), _props);
		entry.setAutoCommit(_autoCommit);
		entry.setSystemConnection(isSystem);
		entry.connect();
		return entry;
	}

	/**
	 * Gets a JDBC connection from the connection pool. The size of the connection pool will be increased if the pool is
	 * full but maxSize has not been reached.
	 * @return the JDBC connection
	 * @throws IllegalStateException if the connection pool is not connected to the JDBC data source
	 * @throws ConnectionPoolException if the connection pool is entirely in use
	 */
	public synchronized Connection getConnection() throws ConnectionPoolException {
		checkConnected();

		// Loop through the connection pool, if we find one not in use then get
		// it
		for (Iterator i = _cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();

			// If the connection pool entry is stale, release it
			if (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME) {
				log.warn("Releasing stale JDBC Connection " + cpe);
				cpe.free();
			}

			// If the connection pool entry is not in use, reserve it
			if (!cpe.inUse()) {
				log.debug("Reserving JDBC Connection " + cpe);
				return cpe.reserve();
			}
		}

		// If we haven't found a free connection, check if we can grow the pool
		if (_cons.size() >= _poolMaxSize)
			throw new ConnectionPoolException("Connection Pool full");

		// Get a new connection and add it to the pool
		try {
			ConnectionPoolEntry cpe = createConnection(false);
			_cons.add(cpe);

			// Return back the new connection
			log.info("Reserving JDBC Connection " + cpe);
			return cpe.reserve();
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}
	}

	/**
	 * Gets a JDBC connection from the connection pool, without failing if the pool is full. This method should only
	 * ever be called by system services that require guaranteed access to the connection pool.
	 * @return the JDBC connection
	 * @throws ConnectionPoolException if a JDBC error occurs creating a new connection
	 */
	public synchronized Connection getSystemConnection() throws ConnectionPoolException {
		checkConnected();

		// Loop through the system connection pool, if we find one not in use
		// then get it
		for (Iterator i = _sysCons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();

			// If the connection pool entry is stale, release it
			if (cpe.getUseTime() > MAX_USE_TIME) {
				log.warn("Releasing stale JDBC Connection " + cpe);
				cpe.free();
			}

			// If the connection pool entry is not in use, reserve it
			if (!cpe.inUse()) {
				log.debug("Reserving JDBC Connection " + cpe);
				return cpe.reserve();
			}
		}
		
		// If there are more than 3 system connections, throw an exception
		if (_sysCons.size() > 3)
			throw new ConnectionPoolException("Connection Pool full");

		// If the pool is full, then create a new connection
		try {
			ConnectionPoolEntry cpe = createConnection(true);
			_sysCons.add(cpe);

			// Return back the new connection
			log.debug("Reserving JDBC Connection " + cpe);
			return cpe.reserve();
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}
	}

	/**
	 * Returns a JDBC connection to the connection pool. <i>Since the connection may have been returned back to the pool
	 * in the middle of a failed transaction, all pending writes will be rolled back and the autoCommit property of the
	 * JDBC connection reset.
	 * @param c the JDBC connection to return
	 * @return the number of milliseconds the connection was used for
	 * @throws IllegalStateException if the connection pool is not connected to the JDBC data source
	 */
	public synchronized long release(Connection c) {
		checkConnected();
		if (c == null)
			return 0;

		// Since this connection may have been given to us with pending writes, ROLL THEM BACK, or do a commit if
		// autoCommit=true
		try {
			if (c.getAutoCommit()) {
				c.commit();
			} else {
				c.rollback();
			}
		} catch (SQLException se) { }
		
		// Build a giant list of connections
		Collection cons = new ArrayList(_cons);
		cons.addAll(_sysCons);

		// Find the connection pool entry and free it
		for (Iterator i = cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();
			if (cpe.equals(c)) {
				cpe.free();
				log.debug("Released JDBC Connection " + cpe + " after " + cpe.getUseTime() + "ms");
				
				// If we have multiple system connections, close this one down
				if (cpe.isSystemConnection() && (_sysCons.size() > 1) && (_sysCons.indexOf(cpe) > 0)) {
					cpe.close();
					i.remove();
				}

				return cpe.getUseTime();
			}
		}

		// If we got this far, the connection was not part of the pool.
		throw new IllegalArgumentException("Invalid JDBC Connection returned");
	}

	/**
	 * Connects the pool to the JDBC data source.
	 * @param initialSize the initial number of connections to establish
	 * @throws IllegalArgumentException if initialSize is negative or greater than getMaxSize()
	 * @throws ConnectionPoolException if a JDBC error occurs
	 */
	public void connect(int initialSize) throws ConnectionPoolException {
		if ((initialSize < 0) || (initialSize > _poolMaxSize))
			throw new IllegalArgumentException("Invalid pool size - " + initialSize);

		// Create connections
		_cons = new ArrayList();
		try {
			for (int x = 0; x < initialSize; x++)
				_cons.add(createConnection(false));

			// Create a system connection
			_sysCons = new ArrayList();
			_sysCons.add(createConnection(true));
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}

		// Start the Connection Monitor
		_monitor.setPool(_cons);
		_monitor.addPool(_sysCons);
		_monitor.start();
	}

	/**
	 * Disconnects the Connection pool from the JDBC data source.
	 */
	public synchronized void close() {
		// Shut down the monitor
		ThreadUtils.kill(_monitor, 500);

		// Disconnect the regular connections
		if (_cons != null) {
			for (Iterator i = _cons.iterator(); i.hasNext();) {
				ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();
				if (cpe.inUse())
					log.warn("Forcibly closing JDBC Connection " + cpe);

				try {
					cpe.getConnection().close();
				} catch (SQLException se) {
					log.warn("Error closing JDBC Connection " + cpe + " - " + se.getMessage());
				}

				log.info("Closed JDBC Connection " + cpe);
			}

			_cons = null;
		}

		// Disconnect the system connections
		if (_sysCons != null) {
			for (Iterator i = _sysCons.iterator(); i.hasNext();) {
				ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();
				if (cpe.inUse())
					log.warn("Forcibly closing JDBC Connection " + cpe);

				try {
					cpe.getConnection().close();
				} catch (SQLException se) {
					log.warn("Error closing JDBC Connection " + cpe + " - " + se.getMessage());
				}

				log.info("Closed JDBC Connection " + cpe);
			}

			_sysCons = null;
		}
	}
	
	/**
	 * Returns information about the connection pool.
	 * @return a Collection of ConnectionInfo entries
	 */
	public Collection getPoolInfo() {
	   // Build a list of all connections
	   Collection allCons = new ArrayList(_cons);
	   allCons.addAll(_sysCons);
	   
	   Set results = new TreeSet();
	   for (Iterator i = allCons.iterator(); i.hasNext(); ) {
	      ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();
	      results.add(new ConnectionInfo(cpe));
	   }
	   
	   return results;
	}
}
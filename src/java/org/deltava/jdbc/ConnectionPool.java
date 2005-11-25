package org.deltava.jdbc;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.util.*;

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
	static final int MAX_USE_TIME = 150 * 1000;
	static final int MAX_SYS_CONS = 3;
	
	private int _poolMaxSize = 1;
	private int _maxRequests;
	private long _totalRequests;

	private ConnectionMonitor _monitor;
	private SortedSet<ConnectionPoolEntry> _cons;

	private Properties _props;
	private boolean _autoCommit = true;

	/**
	 * Creates a new JDBC connection pool.
	 * @param maxSize the maximum size of the connection pool
	 */
	public ConnectionPool(int maxSize) {
		super();
		DriverManager.setLoginTimeout(4);
		_props = new Properties();
		_poolMaxSize = maxSize;
		_monitor = new ConnectionMonitor(3);
	}

	private void checkConnected() throws IllegalStateException {
		if (_cons == null)
			throw new IllegalStateException("Pool not connected");

		// Check that connection monitor is still alive
		if ((_monitor != null) && (!_monitor.isAlive())) {
			log.warn("Connection Monitor not running!");
			_monitor.start();
		}
	}

	/**
	 * Get first available connection ID.
	 */
	private int getNextID() {
		if (CollectionUtils.isEmpty(_cons))
			return 1;
 
		ConnectionPoolEntry cpe = _cons.last();
		return cpe.getID() + 1;
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
	 * Sets the maximum number of reservations of a JDBC Connection. After the maximum
	 * number of reservations have been made, the Connection is closed and another one
	 * opened in its place.
	 * @param maxReqs the maximum number of reuqests, or 0 to disable
	 */
	public void setMaxRequests(int maxReqs) {
		_maxRequests = maxReqs;
	}

	/**
	 * Sets multiple JDBC connection properties at once.
	 * @param props the properties to set
	 */
	public void setProperties(Map<? extends Object, ? extends Object> props) {
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
	 * @param id the Connection ID
	 * @throws SQLException if a JDBC error occurs connecting to the data source.
	 */
	protected ConnectionPoolEntry createConnection(boolean isSystem, int id) throws SQLException {
		log.info("Connecting to " + getURL() + " as user " + _props.getProperty("user", "") + " ID #"
				+ ((isSystem) ? "SYS" : "") + id);
		ConnectionPoolEntry entry = new ConnectionPoolEntry(id, getURL(), _props);
		entry.setAutoCommit(_autoCommit);
		entry.setSystemConnection(isSystem);
		entry.connect();
		return entry;
	}

	/**
	 * Gets a user JDBC connection.
	 * @return a JDBC connection
	 * @throws ConnectionPoolException if the pool is full
	 * @see ConnectionPool#getConnection(boolean)
	 */
	public Connection getConnection() throws ConnectionPoolException {
		return getConnection(false);
	}
	
	/**
	 * Gets a JDBC connection from the connection pool. The size of the connection pool will be increased if the pool is
	 * full but maxSize has not been reached.
	 * @param isSystem TRUE if a system connection is requested, otherwise FALSE
	 * @return the JDBC connection
	 * @throws IllegalStateException if the connection pool is not connected to the JDBC data source
	 * @throws ConnectionPoolException if the connection pool is entirely in use
	 */
	public synchronized Connection getConnection(boolean isSystem) throws ConnectionPoolException {
		checkConnected();

		// Loop through the connection pool, if we find one not in use then get it
		int sysConSize = 0;
		for (Iterator<ConnectionPoolEntry> i = _cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			if (cpe.isSystemConnection())
				sysConSize++;

			// If the connection pool entry is stale, release it
			if (cpe.getUseTime() > MAX_USE_TIME) {
				log.warn("Releasing stale JDBC Connection " + cpe);
				cpe.free();
			}

			// If the connection pool entry is not in use, reserve it
			if (!cpe.inUse() && (cpe.isSystemConnection() == isSystem)) {
				log.debug("Reserving JDBC Connection " + cpe);
				_totalRequests++;
				return cpe.reserve();
			}
		}

		// If we haven't found a free connection, check if we can grow the pool
		if (isSystem && (sysConSize >= MAX_SYS_CONS)) {
			throw new ConnectionPoolFullException();
		} else if (!isSystem && ((_cons.size() - sysConSize) >= _poolMaxSize)) {
			throw new ConnectionPoolFullException();
		}

		// Get a new connection and add it to the pool
		try {
			ConnectionPoolEntry cpe = createConnection(isSystem, getNextID());
			cpe.setDynamic(true);
			_cons.add(cpe);

			// Return back the new connection
			log.info("Reserving JDBC Connection " + cpe);
			_totalRequests++;
			return cpe.reserve();
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}
	}

	/**
	 * Returns a JDBC connection to the connection pool. <i>Since the connection may have been returned back to the pool
	 * in the middle of a failed transaction, all pending writes will be rolled back and the autoCommit property of the
	 * JDBC connection reset.</i>
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
		} catch (SQLException se) {
		}

		// Find the connection pool entry and free it
		for (Iterator<ConnectionPoolEntry> i = _cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();

			// If we find the connection, release it
			if (cpe.equals(c)) {
				cpe.free();

				// If this is a dynamic connection, such it down
				if (cpe.isDynamic()) {
					cpe.close();
					i.remove();
					log.info("Closed dynamic JDBC Connection " + cpe + " after " + cpe.getUseTime() + "ms");
				} else {
					log.debug("Released JDBC Connection " + cpe + " after " + cpe.getUseTime() + "ms");

					// Check if we need to restart
					if ((_maxRequests > 0) && (cpe.getUseCount() > _maxRequests)) {
						log.warn("Restarting JDBC Connection " + cpe + " after " + cpe.getUseCount() + " reservations");
						cpe.close();
						i.remove();
						
						// Create the new Connection
						try {
							_cons.add(createConnection(cpe.isSystemConnection(), cpe.getID()));
						} catch (SQLException se) {
							log.error("Cannot reconnect JDBC Connection " + cpe, se);
						}
					}
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
	public synchronized void connect(int initialSize) throws ConnectionPoolException {
		if ((initialSize < 0) || (initialSize > _poolMaxSize))
			throw new IllegalArgumentException("Invalid pool size - " + initialSize);

		// Create connections
		_cons = new TreeSet<ConnectionPoolEntry>();
		try {
			for (int x = 0; x < initialSize; x++) {
				 ConnectionPoolEntry cpe = createConnection(false, x + 1);
				 _monitor.addConnection(cpe);
				_cons.add(cpe);
			}

			// Create a system connection
			ConnectionPoolEntry sysc = createConnection(true, getNextID());
			_monitor.addConnection(sysc);
			_cons.add(sysc);
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}

		// Start the Connection Monitor
		_monitor.start();
	}

	/**
	 * Disconnects the Connection pool from the JDBC data source.
	 */
	public synchronized void close() {
		// Shut down the monitor
		ThreadUtils.kill(_monitor, 500);

		// Disconnect the regular connections
		if (_cons == null)
			return;

		for (Iterator<ConnectionPoolEntry> i = _cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			if (cpe.inUse())
				log.warn("Forcibly closing JDBC Connection " + cpe);

			try {
				cpe.getConnection().close();
			} catch (SQLException se) {
				log.warn("Error closing JDBC Connection " + cpe + " - " + se.getMessage());
			} finally {
				_monitor.removeConnection(cpe);
			}

			log.info("Closed JDBC Connection " + cpe);
		}

		_cons = null;
	}

	/**
	 * Returns information about the connection pool.
	 * @return a Collection of ConnectionInfo entries
	 */
	public Collection<ConnectionInfo> getPoolInfo() {
		Collection<ConnectionInfo> results = new ArrayList<ConnectionInfo>();
		for (Iterator<ConnectionPoolEntry> i = _cons.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			results.add(new ConnectionInfo(cpe));
		}

		return results;
	}
	
	/**
	 * Returns the total number of connections handed out by the Connection Pool.
	 * @return the number of connection reservations
	 */
	public long getTotalRequests() {
		return _totalRequests;
	}
}
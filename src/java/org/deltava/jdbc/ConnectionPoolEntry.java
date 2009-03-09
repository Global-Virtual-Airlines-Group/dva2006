// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * A class to store JDBC connections in a connection pool and track usage.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

class ConnectionPoolEntry implements java.io.Serializable, Comparable<ConnectionPoolEntry> {

	private static transient final Logger log = Logger.getLogger(ConnectionPoolEntry.class);

	private transient Connection _c;
	private StackTrace _stackInfo;
	private int _id;

	private transient final Properties _props = new Properties();
	private transient String _validationQuery = "SELECT 1";

	private boolean _inUse = false;
	private boolean _systemOwned = false;
	private boolean _dynamic = false;
	private boolean _connected = false;
	private boolean _autoCommit = true;

	private long _totalTime;
	private long _useTime;
	private long _startTime;
	private long _lastUsed;
	private int _useCount;

	/**
	 * Create a new Connection Pool entry.
	 * @param id the connection pool entry ID
	 * @param props JDBC connection properties
	 */
	ConnectionPoolEntry(int id, Properties props) {
		super();
		_id = id;
		if (props.containsKey("validationQuery")) {
			_validationQuery = props.getProperty("validationQuery");
			props.remove("validationQuery");
		}

		_props.putAll(props);
	}

	/**
	 * Check if the connection is in use.
	 * @return TRUE if this connection is in use, otherwise FALSE
	 */
	public boolean inUse() {
		return _inUse;
	}

	/**
	 * Returns whether the Connection is active. Inactivte entries are retained for statistics purposes.
	 * @return TRUE if there is a connection, otherwise FALSE
	 */
	boolean isActive() {
		return (_c != null);
	}

	/**
	 * Returns if this connection is used by the system.
	 * @return TRUE if the connection is system-owned, otherwise FALSE.
	 * @see ConnectionPoolEntry#setSystemConnection(boolean)
	 */
	public boolean isSystemConnection() {
		return _systemOwned;
	}

	/**
	 * Returns if this connection can be reconnected by a connection monitor, or freed after use.
	 * @return TRUE if the connection can be freed after use, otherwise FALSE
	 * @see ConnectionPoolEntry#isSystemConnection()
	 */
	public boolean isDynamic() {
		return _dynamic;
	}

	/**
	 * Returns if the entry is connected to the database. Ordinarily one could check if there was a Connection object in
	 * this class, but since Connections are not {@link java.io.Serializable}, this method would not be valid across web
	 * applications.
	 * @return TRUE if connected, otherwise FALSE
	 * @see ConnectionPoolEntry#connect()
	 * @see ConnectionPoolEntry#close()
	 */
	public boolean isConnected() {
		return ((_c != null) || _connected);
	}

	/**
	 * Connects this entry to the JDBC data source.
	 * @throws SQLException if a JDBC error occurs
	 * @throws IllegalStateException if the entry is already connected
	 */
	void connect() throws SQLException {
		if ((_c != null) && !_c.isClosed())
			throw new IllegalStateException("Connection " + toString() + " already Connected");

		// Create the connection
		Connection c = DriverManager.getConnection(_props.getProperty("url"), _props);
		c.setAutoCommit(_autoCommit);
		c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		_c = new ConnectionWrapper(_id, c);
		_lastUsed = System.currentTimeMillis();
		_connected = true;
	}

	/**
	 * Closes the JDBC connection, swallowing any errors.
	 */
	void close() {
		try {
			_c.close();
		} catch (Exception e) {
			// empty
		} finally {
			_connected = false;
			_c = null;
		}
	}

	/**
	 * Return the connection to the connection pool.
	 */
	void free() {
		if (!inUse())
			return;

		// Reset auto-commit property
		try {
			if (_c.getAutoCommit() != _autoCommit)
				log.info("Resetting autoCommit to " + String.valueOf(_autoCommit));
				
			_c.setAutoCommit(_autoCommit);
			_c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (Exception e) {
			log.error("Error resetting autoCommit/isolation - " + e.getMessage());
		}

		// Add the usage time to the total for this connection
		_useTime = getUseTime();
		_totalTime += _useTime;
		_inUse = false;
	}

	/**
	 * Checks if the underlying JDBC connection is still connected.
	 * @return TRUE if connected, FALSE if not connected
	 */
	boolean checkConnection() {
		try {
			Statement s = _c.createStatement();
			ResultSet rs = s.executeQuery(_validationQuery);
			rs.close();
			s.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the connection object behind this ConnectionPoolEntry. This is package protected since it should only be
	 * accessed by the equals() method or the connection pool itself
	 * @return the JDBC Connection
	 * @see ConnectionPoolEntry#equals(Object)
	 */
	Connection getConnection() {
		return new ConnectionWrapper(_id, _c);
	}

	/**
	 * Returns the Conenction Pool Entry id.
	 * @return the entry id
	 */
	public int getID() {
		return _id;
	}

	/**
	 * Sets the automatic commit setting for this connection. When set, all transactions will be committed to the JDBC
	 * data source immediately. Data Access Objects may change the autoCommit property of the underlying JDBC
	 * connection, but when the connection is returned to the pool its autoCommit property will be reset back to this
	 * value.
	 * @param commit TRUE if connections should autoCommit by default, otherwise FALSE
	 * @see Connection#setAutoCommit(boolean)
	 */
	void setAutoCommit(boolean commit) {
		_autoCommit = commit;
	}

	/**
	 * Marks this connection as being owned by the system. System-owned connections are not used for direct user
	 * queries, instead for application components that need guaranteed access to a JDBC connection.
	 * @param isOwnedBySystem TRUE if this is a system-owned connection, otherwise FALSE
	 * @see ConnectionPoolEntry#isSystemConnection()
	 */
	void setSystemConnection(boolean isOwnedBySystem) {
		_systemOwned = isOwnedBySystem;
	}

	/**
	 * Marks this connection as dynamic.
	 * @param dynamic TRUE if the connection is dynamic, otherwise FALSE
	 * @see ConnectionPoolEntry#isDynamic()
	 */
	void setDynamic(boolean dynamic) {
		_dynamic = dynamic;
	}

	/**
	 * Reserve this Connection pool entry, and get the underlyig JDBC connection. This method is package private since
	 * it only should be called by the ConnectionPool object. If the connection has been disconnected, then an attempt
	 * to reconnect will be made.
	 * @param logStack wether the current thread's stack state should be preserved
	 * @return the JDBC Connection object
	 * @throws ConnectionPoolException if we cannot reconnect
	 * @throws IllegalStateException if the connection is already reserved
	 */
	Connection reserve(boolean logStack) throws ConnectionPoolException {
		if (inUse())
			throw new IllegalStateException("Connection " + toString() + " already in use");

		// If we're not connected, reconnect
		if (!isActive()) {
			try {
				connect();
			} catch (SQLException se) {
				throw new ConnectionPoolException(se);
			}
		}

		// Generate a dummy stack trace if necessary, trimming out entries from this package
		if (logStack) {
			try {
				_stackInfo = new StackTrace();
				_stackInfo.fillInStackTrace();
				List<StackTraceElement> el = Arrays.asList(_stackInfo.getStackTrace());
				StackTraceElement ste = el.get(0);
				String cpePkg = ConnectionPoolEntry.class.getPackage().getName();
				while (ste.getClassName().startsWith(cpePkg) && (el.size() > 1)) {
					el.remove(0);
					ste = el.get(0);
				}

				// Save the stack trace
				if (el.size() > 1)
					_stackInfo.setStackTrace(el.toArray(new StackTraceElement[0]));
			} catch (Exception e) {
				log.warn("Cannot fetch stack trace - " + e.getMessage());
			}
		}

		// Mark the connection as in use, and return the SQL connection
		_startTime = System.currentTimeMillis();
		_lastUsed = _startTime;
		_inUse = true;
		_useCount++;
		return _c;
	}

	/**
	 * Returns how long this connection was used the last time.
	 * @return the time this Connection Entry was reserved, in milliseconds
	 */
	public long getUseTime() {
		return inUse() ? (System.currentTimeMillis() - _startTime) : _useTime;
	}

	/**
	 * Returns the number of times this connection has been reserved.
	 * @return the number of times reserved
	 */
	public int getUseCount() {
		return _useCount;
	}

	/**
	 * Returns the timestamp of this Connection's last use.
	 * @return the connection's last use timestamp
	 */
	public long getLastUseTime() {
		return _lastUsed;
	}

	/**
	 * Returns how long this connection was used since the Connection pool was started.
	 * @return the total time this Connection Entry was reserved, in milliseconds
	 */
	public long getTotalUseTime() {
		return _totalTime;
	}

	/**
	 * Returns this connection's stack trace data, from the last thread to reserve the Connection.
	 * @return a Throwable whose StackTrace is the thread data
	 * @see Throwable#getStackTrace()
	 */
	public Throwable getStackInfo() {
		return _stackInfo;
	}

	/**
	 * This overrides equals behavior by comparing the underlying connection object. This allows us to get a
	 * ConnectionPoolEntry from the pool when all we get back is the SQL Connection.
	 */
	public boolean equals(Object o2) {
		return (o2 instanceof ConnectionPoolEntry) ? (compareTo((ConnectionPoolEntry) o2) == 0) : false;
	}

	/**
	 * Compares two entries by comparing their ID.
	 */
	public int compareTo(ConnectionPoolEntry e2) {
		return Integer.valueOf(_id).compareTo(Integer.valueOf(e2._id));
	}

	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns a text representation of the Connection ID.
	 * @return the connection ID
	 */
	public final String toString() {
		StringBuilder buf = new StringBuilder("#");
		if (isSystemConnection())
			buf.append("SYS");

		buf.append(String.valueOf(_id));
		return buf.toString();
	}
}
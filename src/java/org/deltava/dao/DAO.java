// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import org.deltava.util.CalendarUtils;

/**
 * A JDBC Data Access Object. DAOs are used to read and write persistent data to JDBC data sources.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DAO {
	
	private static final AtomicLong _queryCount = new AtomicLong();

	/**
	 * The maximum number of rows to return.
	 */
	protected int _queryMax;
	
	/**
	 * The row number at which to start returning results.
	 */
	protected int _queryStart;

	private Connection _c;
	private boolean _commitLevel;
	private boolean _manualCommit = false;

	/**
	 * The query timeout, in seconds.
	 */
	protected int _queryTimeout = 45;

	/**
	 * A prepared statement that can be used to perform SQL queries.
	 */
	protected PreparedStatement _ps;

	/**
	 * Creates a Data Access Object with access to a particular connection. <i>If the autoCommit property for the JDBC
	 * connection is false, we assume that manual commits are being used and any internal transaction management is
	 * automatically disabled.</i>
	 * @param c the JDBC connection to use
	 * @see DAO#setManualCommit(boolean)
	 */
	public DAO(Connection c) {
		super();
		_c = c;
		try {
			setManualCommit(!c.getAutoCommit());
		} catch (Exception e) {
		}
	}

	/**
	 * Converts a date-only JDBC value into a full timestamp. Since the server may be several hours ahead or behind most
	 * web users, a default time of 12 noon is applied (instead of the default midnight value) to prevent spurious date
	 * adjustments.
	 * @param dt a JDBC Date
	 * @return a Java date/time
	 */
	protected java.util.Date expandDate(Date dt) {
		if (dt == null)
			return null;

		// Convert to a calendar - if the hour value is zero, adjust forward by 12 hours
		Calendar cld = CalendarUtils.getInstance(dt);
		if (cld.get(Calendar.HOUR_OF_DAY) == 0)
			cld.add(Calendar.HOUR, 12);

		return cld.getTime();
	}

	/**
	 * Tells the Data Access Object that all transaction control will be handled by the calling code, for use in
	 * transactions that require calling several DAOs in sequence. Any DAO commit or rollback operations will not have
	 * any effect.
	 * @param doManual TRUE if the calling code handles commits/rollbacks, otherwise FALSE
	 */
	public void setManualCommit(boolean doManual) {
		_manualCommit = doManual;
	}

	/**
	 * Sets the timeout for any SQL operations
	 * @param timeout the timeout, in seconds
	 * @throws NullPointerException if the prepared statement has not been initialized
	 */
	public void setQueryTimeout(int timeout) {
		if (timeout < 1)
			throw new IllegalArgumentException("Query Timeout cannot be zero or negative");

		_queryTimeout = timeout;
	}

	/**
	 * Sets the first row of the results to return. <i>mySQL uses 0 as its first row. </i>
	 * @param rowStart the first row number of the unfiltered resultset to return
	 * @throws IllegalArgumentException if rowStart is negative
	 */
	public final void setQueryStart(int rowStart) {
		if (rowStart < 0)
			throw new IllegalArgumentException("DAO Start Row cannot be negative");

		_queryStart = rowStart;
	}

	/**
	 * Sets the maximum number of rows in the returned result set.
	 * @param maxRows the maximum number of rows to return
	 * @throws IllegalArgumentException if maxRows is negative
	 */
	public final void setQueryMax(int maxRows) {
		if (maxRows < 0)
			throw new IllegalArgumentException("DAO Query Max cannot be negative");

		_queryMax = maxRows;
	}

	/**
	 * Initialize the prepared statement with an arbitrary SQL statement. This statement appends LIMIT start,max to the
	 * SQL before preparing the statement if this is a SELECT statement.
	 * @param sql the SQL statement to initialize the prepared statement with
	 * @throws SQLException if the prepared statement is invalid
	 * @throws NullPointerException if the SQL string is null
	 * @see DAO#prepareStatementWithoutLimits(String)
	 */
	protected void prepareStatement(String sql) throws SQLException {

		// Build the SQL statement with the limits if we are doing a select
		if (sql.startsWith("SELECT")) {
			StringBuilder buf = new StringBuilder(sql);
			if (_queryMax > 0) {
				buf.append(" LIMIT ");
				buf.append(String.valueOf(_queryMax));
			}

			if (_queryStart > 0) {
				buf.append(" OFFSET ");
				buf.append(String.valueOf(_queryStart));
			}

			// Prepare the statement
			_ps = _c.prepareStatement(buf.toString());
		} else {
			_ps = _c.prepareStatement(sql);
		}

		// Set the query timeout and fetch size
		_ps.setQueryTimeout(_queryTimeout);
		_ps.setFetchSize(_queryMax > 100 ? 100 : _queryMax);
		_queryCount.incrementAndGet();
	}

	/**
	 * Initialize the prepared statement with an abitrary SQL statement, without applying the DAO's query result
	 * limitations. This is useful where a DAO might make multiple queries on the database and only needs to limit a
	 * subset of these queries
	 * @param sql the SQL statement to initialize the prepared statement with
	 * @throws SQLException if the prepared statement is invalid
	 * @see DAO#prepareStatement(String)
	 */
	protected void prepareStatementWithoutLimits(String sql) throws SQLException {
		_ps = _c.prepareStatement(sql);
		_ps.setQueryTimeout(_queryTimeout);
		_ps.setFetchSize(100);
		_queryCount.incrementAndGet();
	}

	/**
	 * Converts a Java date/time value into a JDBC timestamp. This method is null-safe.
	 * @param dt the date/time
	 * @return the JDBC timestamp, or null if dt is null
	 */
	protected Timestamp createTimestamp(java.util.Date dt) {
		return (dt == null) ? null : new Timestamp(dt.getTime());
	}

	/**
	 * Executes an UPDATE transaction on the prepared statement, and throws a {@link SQLException}if less than the
	 * expected number of rows were updated. The prepared statement is closed in either circumstance.
	 * @param minUpdateCount the minimum number of rows to update
	 * @return the actual number of rows updated
	 * @throws SQLException if the update fails due to a JDBC error, or if less than the expected number of rows were
	 * updated
	 */
	protected int executeUpdate(int minUpdateCount) throws SQLException {
		int rowsUpdated = _ps.executeUpdate();
		_ps.close();
		_ps = null;

		// Check if we've updated the expected number of rows
		if ((rowsUpdated >= 0) && (rowsUpdated < minUpdateCount))
			throw new SQLException("Unexpected Row Update count - " + rowsUpdated + ", expected " + minUpdateCount);

		return rowsUpdated;
	}

	/**
	 * Returns the AUTO_INC column value generated by the previous JDBC transaction. This is useful when inserting a new
	 * record into a table where the primary key is a MEDIUMINT AUTO_INCREMENT column and we want to get the new
	 * database ID of the inserted object.
	 * @return the database ID, or 0 if <i>LAST_INSERT_ID() </i> returns null
	 * @throws SQLException if a JDBC error occurs
	 */
	protected int getNewID() throws SQLException {

		// Get the new thread ID
		Statement s = _c.createStatement();
		ResultSet rs = s.executeQuery("SELECT LAST_INSERT_ID()");
		int threadID = rs.next() ? rs.getInt(1) : 0;

		// Clean up and return
		rs.close();
		s.close();
		s = null;
		return threadID;
	}
	
	/**
	 * Formats a database name by converting to lowercase. This method will also return the database name component of
	 * a table name expressed in DB.TABLE format.
	 * @param db the database/table name
	 * @return the converted database name
	 */
	protected String formatDBName(String db) {
		int ofs = db.indexOf('.');
		if (ofs == -1)
			return db.toLowerCase();
		
		return db.substring(0, ofs).toLowerCase();
	}

	/**
	 * Marks the start of a multi-step database transaction. This turns off the autoCommit property of the JDBC
	 * connection, if it is already set.
	 * @throws SQLException if a JDBC error occurs.
	 * @see DAO#commitTransaction()
	 * @see DAO#rollbackTransaction()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void startTransaction() throws SQLException {
		if (!_manualCommit) {
			_commitLevel = _c.getAutoCommit();
			_c.setAutoCommit(false);
		}
	}

	/**
	 * Commits a multi-step transaction to the database. This calls {@link Connection#commit()}on the JDBC connection,
	 * and then restores the old autoCommit property for the Connection.
	 * @throws SQLException if a JDBC error occurs
	 * @see DAO#startTransaction()
	 * @see DAO#rollbackTransaction()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void commitTransaction() throws SQLException {
		if (!_manualCommit) {
			_c.commit();
			_c.setAutoCommit(_commitLevel);
		}
	}

	/**
	 * Rolls back a multi-step transaction before it is completed. This calls {@link Connection#rollback()}on the JDBC
	 * connection, and then resotres the old autoCommit property for the Connection. Since this is designed to be called
	 * in catch blocks, it eats exceptions.
	 * @see DAO#startTransaction()
	 * @see DAO#commitTransaction()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void rollbackTransaction() {
		if (!_manualCommit) {
			try {
				_c.rollback();
				_c.setAutoCommit(_commitLevel);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Returns the total number of queries executed since the JVM was started.
	 * @return the number of queries
	 */
	public final static long getQueryCount() {
		return _queryCount.longValue();
	}
}
// Copyright 2005, 2006, 2007, 2008, 2011, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;

/**
 * A JDBC Data Access Object. DAOs are used to read and write persistent data to JDBC data sources.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public abstract class DAO {
	
	private static final Logger log = Logger.getLogger(DAO.class);
	private static final LongAdder _queryCount = new LongAdder();

	/**
	 * The maximum number of rows to return.
	 */
	protected int _queryMax;
	
	/**
	 * The row number at which to start returning results.
	 */
	protected int _queryStart;

	private transient final Connection _c;
	private boolean _commitLevel;
	private boolean _manualCommit = false;

	/**
	 * The query timeout, in seconds.
	 */
	protected int _queryTimeout = 45;

	/**
	 * A prepared statement that can be used to perform SQL queries.
	 */
	protected transient PreparedStatement _ps;
	
	/**
	 * The SRID used for geolocation queries.
	 */
	protected static final int GEO_SRID = 3587;

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
			// empty
		}
	}

	/**
	 * Converts a date-only JDBC value into a full timestamp. Since the server may be several hours ahead or behind most
	 * web users, a default time of 12 noon is applied (instead of the default midnight value) to prevent spurious date
	 * adjustments.
	 * @param dt a JDBC Date
	 * @return a Java date/time
	 */
	protected static java.time.Instant expandDate(Date dt) {
		return (dt == null) ? null : Instant.ofEpochMilli(dt.getTime()).plusSeconds(12 * 3600);
	}
	
	/**
	 * Converts a geographic location into a MySQL WKT formatted point.
	 * @param loc the GeoLocation
	 * @return the WKT point
	 */
	protected static String formatLocation(GeoLocation loc) {
		return String.format("POINT(%1$,.4f %2$,.4f)", Double.valueOf(loc.getLatitude()), Double.valueOf(loc.getLongitude()));
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
	 * Sets the timeout for any SQL operations.
	 * @param timeout the timeout, in seconds
	 */
	public void setQueryTimeout(int timeout) {
		_queryTimeout = Math.max(1, timeout);
	}

	/**
	 * Sets the first row of the results to return. <i>mySQL uses 0 as its first row.</i>
	 * @param rowStart the first row number of the unfiltered resultset to return
	 */
	public final void setQueryStart(int rowStart) {
		_queryStart = Math.max(0, rowStart);
	}

	/**
	 * Sets the maximum number of rows in the returned result set.
	 * @param maxRows the maximum number of rows to return
	 */
	public final void setQueryMax(int maxRows) {
		_queryMax = Math.max(1, maxRows);
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
			if (_queryMax > 0)
				buf.append(" LIMIT ").append(String.valueOf(_queryMax));
			if (_queryStart > 0)
				buf.append(" OFFSET ").append(String.valueOf(_queryStart));
			
			prepareStatementWithoutLimits(buf.toString());
		} else
			prepareStatementWithoutLimits(sql);
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
		_ps.setFetchSize((_queryMax == 0) ? 500 : Math.min(250, _queryMax + 10));
		_queryCount.increment();
	}
	
	/**
	 * Null-safe conversion of a Timestamp to an Instant.
	 * @param dt the Timestamp
	 * @return an Instant, or null
	 */
	protected static Instant toInstant(Timestamp dt) {
		return (dt == null) ? null : dt.toInstant();
	}

	/**
	 * Null-safe conversion of an Instant to a Timestamp.
	 * @param i the Instant
	 * @return a Timestamp, or null
	 */
	protected static Timestamp createTimestamp(Instant i) {
		return (i == null) ? null : new Timestamp(i.toEpochMilli());
	}

	/**
	 * Executes an UPDATE transaction on the prepared statement, and throws a {@link SQLException}if less than the
	 * expected number of rows were updated. The prepared statement is closed in either circumstance.
	 * @param minUpdateCount the minimum number of rows to update
	 * @return the actual number of rows updated
	 * @throws SQLException if the update fails due to a JDBC error, or if less than the expected number of rows were updated
	 */
	protected int executeUpdate(int minUpdateCount) throws SQLException {
		try (PreparedStatement ps = _ps) {
			int rowsUpdated = _ps.executeUpdate();
			if (rowsUpdated < minUpdateCount)
				throw new SQLException("Unexpected Update count - " + rowsUpdated + ", expected " + minUpdateCount);

			return rowsUpdated;
		}
	}
	
	/**
	 * Executes an batched UPDATE transaction on the prepared statement, and throws a {@link SQLException}if less than the
	 * expected number of rows were updated per batch entry. The prepared statement is closed in either circumstance.
	 * @param minPerUpdate the minimum number of rows to update per batch entry
	 * @param minTotal the minimum number of rows to update across the entry batch
	 * @return the actual number of rows updated
	 * @throws SQLException if the update fails due to a JDBC error, or if less than the expected number of rows were updated
	 */
	protected int executeBatchUpdate(int minPerUpdate, int minTotal) throws SQLException {
		try (PreparedStatement ps = _ps) {
			int[] rowsUpdated = _ps.executeBatch(); int totalRows = 0;
			for (int x = 0; x < rowsUpdated.length; x++) {
				totalRows += rowsUpdated[x];
				if (rowsUpdated[x] < minPerUpdate)
					throw new SQLException("Unexpected Update count at batch entry " + x + " - " + rowsUpdated[x] + ", expected " + minPerUpdate);
			}
			
			if (totalRows < minTotal)
				throw new SQLException("Unexpected Update count - " + totalRows + ", expected " + minTotal);
			
			return totalRows;
		}
	}

	/**
	 * Returns the AUTO_INC column value generated by the previous JDBC transaction. This is useful when inserting a new
	 * record into a table where the primary key is a MEDIUMINT AUTO_INCREMENT column and we want to get the new
	 * database ID of the inserted object.
	 * @return the database ID, or 0 if <i>LAST_INSERT_ID() </i> returns null
	 * @throws SQLException if a JDBC error occurs
	 */
	protected int getNewID() throws SQLException {
		try (Statement s = _c.createStatement(); ResultSet rs = s.executeQuery("SELECT LAST_INSERT_ID()")) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}
	
	/**
	 * Formats a database name by converting to lowercase. This method will also return the database name component of
	 * a table name expressed in DB.TABLE format.
	 * @param db the database/table name
	 * @return the converted database name
	 */
	protected static String formatDBName(String db) {
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
			} catch (SQLException se) {
				log.error("Cannot rollback - " + se.getMessage(), se);
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
// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.LogEntry;

/**
 * A Data Access Object to read System Log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSystemLog extends DAO {

	private String _logName = "LOG_APP";
	private java.util.Date _startDate;
	private java.util.Date _endDate;
	private int _priority;

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSystemLog(Connection c) {
		super(c);
	}

	/**
	 * Sets the log name to query.
	 * @param name the log name
	 */
	public void setName(String name) {
		_logName = "LOG_" + name;
	}

	/**
	 * Sets the date range to query.
	 * @param sd the start date/time
	 * @param ed the end date/time
	 */
	public void setDateRange(java.util.Date sd, java.util.Date ed) {
		_startDate = sd;
		_endDate = ((ed == null) && (sd != null)) ? new java.util.Date() : ed;
	}

	/**
	 * Sets the log priority to query.
	 * @param p the priority code
	 */
	public void setPriority(int p) {
		_priority = p;
	}

	/**
	 * Gets all Log Entries from a particular System Log table.
	 * @return a List of LogEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getAll() throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(_logName);
		sqlBuf.append(" WHERE (PRIORITY >= (? * 10000))");
		if (_startDate != null)
			sqlBuf.append(" AND (CREATED >= ?) AND (CREATED <= ?)");

		sqlBuf.append(" ORDER BY CREATED DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, _priority);
			if (_startDate != null) {
				_ps.setTimestamp(2, createTimestamp(_startDate));
				_ps.setTimestamp(3, createTimestamp(_endDate));
			}

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Gets all Log Entries from a particular logger or package. This call will perform a LIKE comparison on the logger
	 * name, so passing com.foo will return entries for com.foo.x1 and com.foo.x2.
	 * @param className the logger class name or portion thereof
	 * @return a List of LogEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByType(String className) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(_logName);
		sqlBuf.append(" WHERE (LOGGER LIKE ?) AND (PRIORITY >= ?)");
		if (_startDate != null)
			sqlBuf.append(" AND (CREATED >= ?) AND (CREATED <= ?)");

		sqlBuf.append(" ORDER BY CREATED DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, className + "%");
			_ps.setInt(2, _priority);
			if (_startDate != null) {
				_ps.setTimestamp(3, createTimestamp(_startDate));
				_ps.setTimestamp(4, createTimestamp(_endDate));
			}

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List results = new ArrayList();
		while (rs.next()) {
			LogEntry entry = new LogEntry(rs.getTimestamp(2));
			entry.setID(rs.getInt(1));
			entry.setPriority(rs.getInt(3));
			entry.setClassName(rs.getString(4));
			entry.setMessage(rs.getString(5));
			entry.setError(rs.getString(6));

			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
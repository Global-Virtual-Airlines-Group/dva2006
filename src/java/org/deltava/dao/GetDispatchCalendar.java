// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.DispatchScheduleEntry;

/**
 * A Data Access Object to read ther ACARS Dispatcher service calendar.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */
public class GetDispatchCalendar extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetDispatchCalendar(Connection c) {
		super(c);
	}

	/**
	 * Retrieves a specifc Dispatcher service entry.
	 * @param id the entry database ID
	 * @return a DispatchScheduleEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public DispatchScheduleEntry get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.DSP_SCHEDULE WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			List<DispatchScheduleEntry> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Return Dispatcher service times for the Calendar.
	 * @param sd the start date/time
	 * @param days the number of days to retrieve
	 * @param dispatcherID the dispatcher database ID, or zero if all
	 * @return a Collection of DispatchScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchScheduleEntry> getCalendar(java.util.Date sd, int days, int dispatcherID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM acars.DSP_SCHEDULE ");
		if ((sd != null) || (dispatcherID != 0))
			sqlBuf.append("WHERE ");
		if (sd != null) {
			sqlBuf.append("(STARTTIME >=?) AND (STARTTIME < DATE_ADD(?, INTERVAL ? DAY)) ");
			if (dispatcherID > 0)
				sqlBuf.append("AND ");
		}
		
		if (dispatcherID > 0)
			sqlBuf.append("(ID=?) ");
		sqlBuf.append("ORDER BY STARTTIME");
		
		int param = 0;
		try {
			prepareStatement(sqlBuf.toString());
			if (sd != null) {
				_ps.setTimestamp(++param, createTimestamp(sd));
				_ps.setTimestamp(++param, createTimestamp(sd));
				_ps.setInt(++param, days);
			}
			
			if (dispatcherID > 0)
				_ps.setInt(++param, dispatcherID);
			
			// Execute the query
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<DispatchScheduleEntry> execute() throws SQLException {
		List<DispatchScheduleEntry> results = new ArrayList<DispatchScheduleEntry>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			DispatchScheduleEntry e = new DispatchScheduleEntry(rs.getInt(2));
			e.setStartTime(rs.getTimestamp(3));
			e.setEndTime(rs.getTimestamp(4));
			e.setID(rs.getInt(1));
			e.setComments(rs.getString(5));
			results.add(e);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
 	}
}
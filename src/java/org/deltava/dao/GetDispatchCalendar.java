// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object to read ther ACARS Dispatcher service calendar.
 * @author Luke
 * @version 2.4
 * @since 2.2
 */

public class GetDispatchCalendar extends GetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetDispatchCalendar(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all ACARS Dispatch connection entries within a time span.
	 * @param sd the start date/time
	 * @param days the number of days forward to retrieve
	 * @return a List of dispatch ConnectionEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ConnectionEntry> getDispatchConnections(java.util.Date sd, int days) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET_NTOA(C.REMOTE_ADDR), "
					+ "C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD, C.DISPATCH FROM acars.CONS C WHERE "
					+ "(C.DISPATCH=?) AND (C.DATE > ?) AND (C.DATE < DATE_ADD(?, INTERVAL ? DAY)) ORDER BY C.ID");
			_ps.setBoolean(1, true);
			_ps.setTimestamp(2, createTimestamp(sd));
			_ps.setTimestamp(3, createTimestamp(sd));
			_ps.setInt(4, days);
			return executeConnectionInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Flights dispatched by a Dispatcher during a particular Connection.
	 * @param ce the DispatchConnectionEntry
	 * @return a List of FlightInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightInfo> getDispatchedFlights(DispatchConnectionEntry ce) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT F.*, FD.ROUTE_ID, FD.DISPATCHER_ID, C.PILOT_ID FROM acars.CONS C, "
				+ "acars.FLIGHTS F, acars.FLIGHT_DISPATCH FD WHERE (C.ID=F.CON_ID) AND (F.ID=FD.ID) AND "
				+ "(FD.DISPATCHER_ID=?) AND (F.CREATED > ?) AND (F.CREATED < ?)");
			_ps.setInt(1, ce.getPilotID());
			_ps.setTimestamp(2, createTimestamp(ce.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(ce.getEndTime()));
			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all Flights dispatched by a Dispatcher within a time span.
	 * @param id the Dispatcher's database ID
	 * @param sd the start date/time
	 * @param days the number of days forward to retrieve
	 * @return a List of FlightInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getDispatchedFlights(int id, java.util.Date sd, int days) throws DAOException {
		try {
			prepareStatement("SELECT F.*, FD.ROUTE_ID, FD.DISPATCHER_ID, C.PILOT_ID FROM acars.CONS C, "
				+ "acars.FLIGHTS F, acars.FLIGHT_DISPATCH FD WHERE (C.ID=F.CON_ID) AND (F.ID=FD.ID) AND "
				+ "(FD.DISPATCHER_ID=?) AND (F.CREATED > ?) AND (F.CREATED < DATE_ADD(?, INTERVAL ? DAY))");
			_ps.setInt(1, id);
			_ps.setTimestamp(2, createTimestamp(sd));
			_ps.setTimestamp(3, createTimestamp(sd));
			_ps.setInt(4, days);
			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
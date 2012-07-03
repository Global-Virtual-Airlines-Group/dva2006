// Copyright 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DateRange;
import org.deltava.beans.acars.*;

/**
 * A Data Access Object to read the ACARS Dispatcher service calendar.
 * @author Luke
 * @version 4.2
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
	 * @param dr the DateRange
	 * @return a List of dispatch ConnectionEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ConnectionEntry> getDispatchConnections(DateRange dr) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET_NTOA(C.REMOTE_ADDR), "
				+ "C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD FROM acars.CONS C WHERE (C.DATE > ?) AND "
				+ "(C.DATE < ?) ORDER BY C.ID");
			_ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			_ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
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
			prepareStatementWithoutLimits("SELECT F.*, INET_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, "
				+ "FDR.DISPATCHER_ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) "
				+ "LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) WHERE (FDR.DISPATCHER_ID=?) AND "
				+ "(F.CREATED >= ?) AND (F.CREATED < DATE_ADD(?, INTERVAL ? MINUTE))");
			_ps.setInt(1, ce.getPilotID());
			_ps.setTimestamp(2, createTimestamp(ce.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(ce.getEndTime()));
			_ps.setInt(4, 20);
			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all Flights dispatched by a Dispatcher within a time span.
	 * @param id the Dispatcher's database ID
	 * @param dr the DateRange
	 * @return a List of FlightInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getDispatchedFlights(int id, DateRange dr) throws DAOException {
		try {
			prepareStatement("SELECT F.*, INET_NTOA(F.REMOTE_ADDR),FD.ROUTE_ID, FDR.DISPATCHER_ID FROM "
				+ "acars.FLIGHTS F, acars.FLIGHT_DISPATCHER FDR LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) "
				+ "WHERE (F.ID=FDR.ID) AND (FDR.DISPATCHER_ID=?) AND (F.CREATED >= ?) AND (F.CREATED < ?)");
			_ps.setInt(1, id);
			_ps.setTimestamp(2, createTimestamp(dr.getStartDate()));
			_ps.setTimestamp(3, createTimestamp(dr.getEndDate()));
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
	 * @param dispatcherID the dispatcher database ID, or zero if all
	 * @param dr the DateRange
	 * @return a Collection of DispatchScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchScheduleEntry> getCalendar(int dispatcherID, DateRange dr) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM acars.DSP_SCHEDULE ");
		if ((dr != null) || (dispatcherID != 0))
			sqlBuf.append("WHERE ");
		if (dr != null) {
			sqlBuf.append("(STARTTIME >=?) AND (STARTTIME < ?) ");
			if (dispatcherID > 0)
				sqlBuf.append("AND ");
		}
		
		if (dispatcherID > 0)
			sqlBuf.append("(ID=?) ");
		sqlBuf.append("ORDER BY STARTTIME");
		
		int param = 0;
		try {
			prepareStatement(sqlBuf.toString());
			if (dr != null) {
				_ps.setTimestamp(++param, createTimestamp(dr.getStartDate()));
				_ps.setTimestamp(++param, createTimestamp(dr.getEndDate()));
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
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				DispatchScheduleEntry e = new DispatchScheduleEntry(rs.getInt(2));
				e.setStartTime(rs.getTimestamp(3));
				e.setEndTime(rs.getTimestamp(4));
				e.setID(rs.getInt(1));
				e.setComments(rs.getString(5));
				results.add(e);
			}
		}
		
		_ps.close();
		return results;
 	}
}
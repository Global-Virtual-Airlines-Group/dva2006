// Copyright 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DateRange;
import org.deltava.beans.acars.*;

/**
 * A Data Access Object to read the ACARS Dispatcher service calendar.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET6_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD FROM acars.CONS C "
			+ "WHERE (C.DATE > ?) AND (C.DATE < ?) ORDER BY C.ID")) {
			ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			return executeConnectionInfo(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT F.*, INET6_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, FDR.DISPATCHER_ID, FDL.LOG_ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) "
				+ "LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) LEFT JOIN acars.FLIGHT_DISPATCH_LOG FDL ON (F.ID=FDL.ID) WHERE (FDR.DISPATCHER_ID=?) AND (F.CREATED >= ?) AND (F.CREATED < DATE_ADD(?, INTERVAL ? MINUTE))")) {
			ps.setInt(1, ce.getPilotID());
			ps.setTimestamp(2, createTimestamp(ce.getStartTime()));
			ps.setTimestamp(3, createTimestamp(ce.getEndTime()));
			ps.setInt(4, 20);
			return executeFlightInfo(ps);
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
		try (PreparedStatement ps = prepare("SELECT F.*, INET6_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, FDR.DISPATCHER_ID, FDL.LOG_ID FROM acars.FLIGHT_DISPATCHER FDR, acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD "
			+ "ON (F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCH_LOG FDL ON (F.ID=FDL.ID) WHERE (F.ID=FDR.ID) AND (FDR.DISPATCHER_ID=?) AND (F.CREATED >= ?) AND (F.CREATED < ?)")) {
			ps.setInt(1, id);
			ps.setTimestamp(2, createTimestamp(dr.getStartDate()));
			ps.setTimestamp(3, createTimestamp(dr.getEndDate()));
			return executeFlightInfo(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.DSP_SCHEDULE WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
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
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (dr != null) {
				ps.setTimestamp(++param, createTimestamp(dr.getStartDate()));
				ps.setTimestamp(++param, createTimestamp(dr.getEndDate()));
			}
			
			if (dispatcherID > 0)
				ps.setInt(++param, dispatcherID);
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private static List<DispatchScheduleEntry> execute(PreparedStatement ps) throws SQLException {
		List<DispatchScheduleEntry> results = new ArrayList<DispatchScheduleEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				DispatchScheduleEntry e = new DispatchScheduleEntry(rs.getInt(2));
				e.setStartTime(toInstant(rs.getTimestamp(3)));
				e.setEndTime(toInstant(rs.getTimestamp(4)));
				e.setID(rs.getInt(1));
				e.setComments(rs.getString(5));
				results.add(e);
			}
		}
		
		return results;
 	}
}
// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object used to get information for purging the ACARS data tables. 
 * @author Luke
 * @version 4.1
 * @since 3.2
 */

public class GetACARSPurge extends GetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSPurge(Connection c) {
		super(c);
	}

	/**
	 * Returns all ACARS connection log entries with no associated Flight Info logs or text messages. A cutoff interval
	 * is provided to prevent the accidental inclusion of flights still in progress.
	 * @param cutoff the cutoff interval for connection entries, in hours
	 * @return a List of ConnectionEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ConnectionEntry> getUnusedConnections(int cutoff) throws DAOException {
		try {
			prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, IFNULL(C.ENDDATE, DATE_ADD(C.DATE, INTERVAL 18 HOUR)) "
					+ "AS ED, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD, C.DISPATCH, "
					+ "COUNT(DISTINCT F.ID) AS FC, COUNT(P.FLIGHT_ID) AS PC FROM acars.CONS C LEFT JOIN "
					+ "acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.POSITIONS P ON (F.ID=P.FLIGHT_ID) GROUP BY "
					+ "C.ID HAVING (ED < DATE_SUB(NOW(), INTERVAL ? HOUR)) AND (FC=0) AND (PC=0)");
			_ps.setInt(1, cutoff);
			return executeConnectionInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Information entries without an associated Flight Report. A cutoff interval is provided to
	 * prevent the accidental inclusion of flights still in progress.
	 * @param cutoff the cutoff interval for flight entries, in hours
	 * @return a List of InfoEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getUnreportedFlights(int cutoff) throws DAOException {
		try {
			prepareStatement("SELECT F.*, FD.ROUTE_ID, FDR.DISPATCHER_ID, C.PILOT_ID FROM acars.FLIGHTS F "
					+ "LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON "
					+ "(F.ID=FDR.ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (F.PIREP=?) AND (F.ARCHIVED=?) "
					+ "AND (F.CREATED < DATE_SUB(NOW(), INTERVAL ? HOUR)) ORDER BY F.CREATED");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, cutoff);
			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves Flight IDs for position entries older than a particular number of days.
	 * @param days the number of days
	 * @return a Collection of ACARS Flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPositionFlightIDs(int days) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT FLIGHT_ID FROM acars.POSITIONS WHERE (REPORT_TIME < DATE_SUB(NOW(), INTERVAL ? DAY))");
			_ps.setInt(1, days);
			
			// Execute the query
			Collection<Integer> results = new TreeSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of all ACARS flights marked as archived with unarchived position entries.
	 * @return a Collection of flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getUnsynchedACARSFlights() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT P.FLIGHT_ID FROM acars.POSITIONS P, acars.FLIGHTS F WHERE "
					+ "(F.ARCHIVED=?) AND (P.FLIGHT_ID=F.ID)");
			_ps.setBoolean(1, true);
			
			// Get ACARS flight IDs
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}

			_ps.close();
			return IDs;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of all ACARS flights marked as archived with unarchived position entries.
	 * @return a Collection of flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getUnsynchedXACARSFlights() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT XP.FLIGHT_ID FROM acars.POSITION_XARCHIVE XP, acars.FLIGHTS F "
				+ "WHERE (F.ARCHIVED=?) AND (P.FLIGHT_ID=F.ID)");
			_ps.setBoolean(1, true);
			
			// Get XACARS flight IDs
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}

			_ps.close();
			return IDs;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
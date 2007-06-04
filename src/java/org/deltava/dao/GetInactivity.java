// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.FlightReport;
import org.deltava.beans.system.InactivityPurge;

/**
 * A Data Access Object to read Inactivity purge entries. This DAO extends PilotReadDAO since it is used
 * to query pilots who may not have an Inactivity purge table entry, but are eligible for one.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetInactivity extends PilotReadDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetInactivity(Connection c) {
		super(c);
	}

	/**
	 * Returns inactivity data for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return an InactivityPurge bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public InactivityPurge getInactivity(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM INACTIVITY WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			
			// Execute query, return null if empty 
			List<InactivityPurge> results = executeInactivity();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns purge entries for all pilots who are eligible to be purged.
	 * @param isNotified TRUE if the Pilots should have been notified, otherwise FALSE  
	 * @return a Collection of InactivityPurge beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InactivityPurge> getPurgeable(boolean isNotified) throws DAOException {
		try {
			prepareStatement("SELECT * FROM INACTIVITY WHERE (NOTIFY=?) AND (PURGE_DATE < CURDATE())");
			_ps.setBoolean(1, isNotified);
			return executeInactivity();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns purge entries for all pilots who are eligible to be purged.
	 * @param days the number of days in the future
	 * @return a Collection of InactivityPurge beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InactivityPurge> getPurgeableSoon(int days) throws DAOException {
		try {
			prepareStatement("SELECT * FROM INACTIVITY WHERE (PURGE_DATE < DATE_ADD(CURDATE(), INTERVAL ? DAY))");
			_ps.setInt(1, days);
			return executeInactivity();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Pilots with no Flight Reports in a set number of days. This call can be used to purge pilots
	 * based on no flying activity, and can set different thresholds for different levels of total Flights.
	 * @param days the number of days with no Flight Reports
	 * @param flights the maximum number of total Flights for each Pilot
	 * @return a Collection of Pilot beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getNoFlightPilots(int days, int flights) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE) AS LF FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
					+ "(P.STATUS=?) GROUP BY P.ID HAVING (LEGS <= ?) AND ((DATE_ADD(LF, INTERVAL ? DAY) < CURDATE()) "
					+ "OR (LF IS NULL))");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Pilot.ACTIVE);
			_ps.setInt(3, flights);
			_ps.setInt(4, days);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Pilots who have not logged in for a certain number of days.
	 * @param days the number of days
	 * @return a Collection of Pilot beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getInactivePilots(int days) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
					+ "(P.STATUS=?) AND (DATE_ADD(P.LAST_LOGIN, INTERVAL ? DAY) < CURDATE()) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Pilot.ACTIVE);
			_ps.setInt(3, days);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Inactivity result sets.
	 */
	private List<InactivityPurge> executeInactivity() throws SQLException {
		List<InactivityPurge> results = new ArrayList<InactivityPurge>();
		
		// Iterate through the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			InactivityPurge ip = new InactivityPurge(rs.getInt(1));
			ip.setPurgeDate(expandDate(rs.getDate(2)));
			ip.setNotify(rs.getBoolean(3));
			
			// Add to results
			results.add(ip);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
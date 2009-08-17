// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.6
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
	 * Returns the IDs of Pilots who have not logged in for a certain number of days.
	 * @param days the number of days
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getInactivePilots(int days) throws DAOException {
		try {
			prepareStatement("SELECT ID FROM PILOTS WHERE (STATUS=?) AND (DATE_ADD(IFNULL(LAST_LOGIN, CREATED), "
					+ "INTERVAL ? DAY) < CURDATE())");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Pilot.ACTIVE);
			_ps.setInt(3, days);
			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of Pilots who have not participated in the period prior to the Inactivity warning.
	 * @param loginDays the number of days since their last login
	 * @param activityDays the number of days to check for Flight Reports or Cooler posts
	 * @param minPosts the minimum number of Cooler posts
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getRepeatInactive(int loginDays, int activityDays, int minPosts) throws DAOException {
		try {
			prepareStatement("SELECT P.ID, COUNT(F.ID) AS FLIGHTS, COUNT(CP.POST_ID) AS POSTS FROM PILOTS P LEFT JOIN "
					+ "PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?) AND (F.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))) LEFT JOIN "
					+ "common.COOLER_POSTS CP ON ((P.ID=CP.AUTHOR_ID) AND (CP.CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))) "
					+ "WHERE (P.STATUS=?) AND P.LAST_LOGIN < DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY P.ID HAVING (FLIGHTS=0) "
					+ "AND (POSTS<?)");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, activityDays);
			_ps.setInt(3, activityDays);
			_ps.setInt(4, Pilot.ACTIVE);
			_ps.setInt(5, loginDays);
			_ps.setInt(6, minPosts);
			return executeIDs();
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
			ip.setInterval(rs.getInt(4));
			results.add(ip);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
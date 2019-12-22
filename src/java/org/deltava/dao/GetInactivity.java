// Copyright 2005, 2007, 2009, 2011, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.system.InactivityPurge;

/**
 * A Data Access Object to read Inactivity purge entries. This DAO extends PilotReadDAO since it is used
 * to query pilots who may not have an Inactivity purge table entry, but are eligible for one.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT * FROM INACTIVITY WHERE (ID=?)")) {
			ps.setInt(1, pilotID);
			return executeInactivity(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns purge entries for all pilots who are eligible to be purged.
	 * @return a Collection of InactivityPurge beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InactivityPurge> getPurgeable() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT I.* FROM INACTIVITY I, PILOTS P WHERE (P.ID=I.ID) AND (I.PURGE_DATE < CURDATE()) AND ((I.NOTIFY=?) OR (P.LOGINS=?))")) {
			ps.setBoolean(1, true);
			ps.setInt(2, 0);
			return executeInactivity(ps);
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
		try (PreparedStatement ps = prepare("SELECT ID FROM PILOTS WHERE (STATUS=?) AND (DATE_ADD(IFNULL(LAST_LOGIN, DATE_ADD(CREATED, INTERVAL 1 HOUR)), INTERVAL ? DAY) < NOW())")) {
			ps.setInt(1, PilotStatus.ACTIVE.ordinal());
			ps.setInt(2, days);
			return executeIDs(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of Pilots who have not participated in the period prior to the Inactivity warning.
	 * @param loginDays the number of days since their last login
	 * @param activityDays the number of days to check for Flight Reports, Cooler posts or Examinations
	 * @param minPosts the minimum number of Cooler posts
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getRepeatInactive(int loginDays, int activityDays, int minPosts) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.ID, COUNT(F.ID) AS FLIGHTS, COUNT(CP.POST_ID) AS POSTS, COUNT(E.ID) AS EXAMS FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) "
			+ "AND (F.STATUS=?) AND (F.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))) LEFT JOIN common.COOLER_POSTS CP ON ((P.ID=CP.AUTHOR_ID) AND (CP.CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))) "
			+ "LEFT JOIN exams.EXAMS E ON ((E.PILOT_ID=P.ID) AND (E.CREATED_ON > DATE_SUB(NOW(), INTERVAL ? DAY))) WHERE (P.STATUS=?) AND P.LAST_LOGIN < DATE_SUB(NOW(), INTERVAL ? DAY) "
			+ "GROUP BY P.ID HAVING (FLIGHTS=0) AND (POSTS<?) AND (EXAMS=0)")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, activityDays);
			ps.setInt(3, activityDays);
			ps.setInt(4, activityDays);
			ps.setInt(5, PilotStatus.ACTIVE.ordinal());
			ps.setInt(6, loginDays);
			ps.setInt(7, minPosts);
			return executeIDs(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Inactivity result sets.
	 */
	private static List<InactivityPurge> executeInactivity(PreparedStatement ps) throws SQLException {
		List<InactivityPurge> results = new ArrayList<InactivityPurge>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				InactivityPurge ip = new InactivityPurge(rs.getInt(1));
				ip.setPurgeDate(expandDate(rs.getDate(2)));
				ip.setNotify(rs.getBoolean(3));
				ip.setInterval(rs.getInt(4));
				results.add(ip);
			}
		}
		
		return results;
	}
}
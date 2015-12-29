// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.Status;

/**
 * A Data Access Object to select dormant Flight Academy courses. 
 * @author Luke
 * @version 6.3
 * @since 6.3
 */

public class GetAcademyInactivity extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyInactivity(Connection c) {
		super(c);
	}

	/**
	 * Returns inactive Flight Academy courses from a particular Airline. This will return Courses that have
	 * had no completed progress, chats or submitted examinations/check rides within the specified interval.
	 * @param interval the interval since last activity
	 * @param dbName the databasae to query
	 * @return a Map of Course IDs and intervals since last activity in days
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Integer> getInactiveCourses(int interval, String dbName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.ID, GREATEST(C.STARTDATE, IFNULL(MAX(CC.CREATED),C.STARTDATE), IFNULL(MAX(CP.COMPLETED), C.STARTDATE), "
				+ "IFNULL(MAX(E.SUBMITTED_ON), C.STARTDATE), IFNULL(MAX(CR.SUBMITTED), C.STARTDATE)) AS LASTPRG FROM common.USERDATA UD, exams.COURSES C "
				+ "LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) LEFT JOIN exams.COURSEPROGRESS CP ON (C.ID=CP.ID) LEFT JOIN exams.CERTEXAMS CE "
				+ "ON (C.CERTNAME=CE.CERTNAME) LEFT JOIN exams.COURSERIDES CCR ON (C.ID=CCR.COURSE) LEFT JOIN exams.CHECKRIDES CR ON (CR.ID=CCR.CHECKRIDE) "
				+ "LEFT JOIN exams.EXAMS E ON ((C.PILOT_ID=E.PILOT_ID) AND (E.NAME=CE.EXAMNAME)) WHERE (UD.ID=C.PILOT_ID) AND (C.STATUS=?) AND (UD.AIRLINE=?) "
				+ "GROUP BY C.ID HAVING (LASTPRG < DATE_SUB(NOW(), INTERVAL ? DAY)) ORDER BY LASTPRG");
			_ps.setInt(1, Status.STARTED.ordinal());
			_ps.setString(2, formatDBName(dbName));
			_ps.setInt(3, interval);
			
			// Execute the query
			Map<Integer, Integer> results = new LinkedHashMap<Integer, Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				long now = System.currentTimeMillis();
				while (rs.next()) {
					Timestamp ts = rs.getTimestamp(2);
					long intervalDays = (now - ts.getTime()) / 86400_000;
					results.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf((int) intervalDays));
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
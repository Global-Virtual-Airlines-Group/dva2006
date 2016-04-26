// Copyright 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object for Flight Academy Course progress beans.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class GetAcademyProgress extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyProgress(Connection c) {
		super(c);
	}

	/**
	 * Loads all Course progress beans for Course Requirements that have a specific examination as a requirement.
	 * @param examName the examination name
	 * @param pilotID the Pilot database ID
	 * @return a Collection of CourseProgress beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CourseProgress> getRequirements(String examName, int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT CP.* FROM exams.COURSEPROGRESS CP, exams.COURSES C WHERE (CP.ID=C.ID) AND "
					+ "(C.STATUS=?) AND (CP.COMPLETE=?) AND (CP.EXAMNAME=?) AND (C.PILOT_ID=?) ORDER BY SEQ");
			_ps.setInt(1, Status.STARTED.ordinal());
			_ps.setBoolean(2, false);
			_ps.setString(3, examName);
			_ps.setInt(4, pilotID);
			Collection<CourseProgress> results = new ArrayList<CourseProgress>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					CourseProgress cp = new CourseProgress(rs.getInt(1), rs.getInt(2));
					cp.setAuthorID(rs.getInt(3));
					cp.setText(rs.getString(4));
					cp.setExamName(rs.getString(5));
					cp.setComplete(rs.getBoolean(6));
					cp.setCompletedOn(toInstant(rs.getTimestamp(7)));
					results.add(cp);
				}
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
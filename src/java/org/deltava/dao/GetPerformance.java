// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import static org.deltava.beans.flight.FlightReport.*;
import static org.deltava.beans.testing.Test.SCORED;

import org.deltava.beans.stats.PerformanceMetrics;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load performance data from the database.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class GetPerformance extends DAO {
	
	private String _categorySQL;

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPerformance(Connection c) {
		super(c);
	}
	
	/**
	 * Sets the SQL used to generate the category. <i>This is unescaped</i>
	 * @param sql the SQL statement.
	 */
	public void setCategorySQL(String sql) {
		_categorySQL = sql;
	}
	
	/**
	 * Returns whether we are grouping by a database ID.
	 * @return TRUE if grouping by a database ID, otherwise FALSE
	 */
	public boolean isPilotID() {
		return _categorySQL.contains("_BY") || _categorySQL.contains("_ID");
	}

	/**
	 * Returns Flight Report approval metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getFlightApproval(int startDays, int endDays) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS AV, "
				+ "MIN(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS MN, "
				+ "MAX(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS MX, COUNT(*) AS CNT FROM "
				+ "PIREPS WHERE (STATUS=?) AND (DATE >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND "
				+ "(DATE <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (SUBMITTED IS NOT NULL) GROUP BY "
				+ "CATNAME ORDER BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, OK);
			_ps.setInt(2, startDays);
			_ps.setInt(3, endDays);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Pilot Examination grading metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getExamGrading(int startDays, int endDays) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS AV, "
				+ "MIN(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS MN, "
				+ "MAX(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS MX, COUNT(E.ID) AS CNT FROM "
				+ "exams.EXAMS E, exams.EXAMINFO EP WHERE (E.NAME=EP.NAME) AND (E.STATUS=?) AND "
				+ "(EP.AIRLINE=?) AND (E.CREATED_ON >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND "
				+ "(E.CREATED_ON <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, SCORED);
			_ps.setString(2, SystemData.get("airline.code"));
			_ps.setInt(3, startDays);
			_ps.setInt(4, endDays);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Pilot Check Ride grading metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getCheckRideGrading(int startDays, int endDays) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS AV, "
				+ "MIN(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS MN, "
				+ "MAX(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS MX, COUNT(CR.ID) AS CNT FROM "
				+ "exams.CHECKRIDES CR, common.EQPROGRAMS EP WHERE (CR.STATUS=?) AND (CR.EQTYPE=EP.EQTYPE) AND "
				+ "(EP.AIRLINE=?) AND (CR.CREATED >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND "
				+ "(CR.CREATED <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (LOCATE(?, CR.NAME) = 0) GROUP BY CATNAME");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, SCORED);
			_ps.setString(2, SystemData.get("airline.code"));
			_ps.setInt(3, startDays);
			_ps.setInt(4, endDays);
			_ps.setString(5, "Initial Hire");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Water Cooler posting metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getCoolerPosts(int startDays, int endDays) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, (COUNT(*) / COUNT(DISTINCT(AUTHOR_ID))) AS AV, "
				+ "(SELECT COUNT(*) AS PCNT FROM common.COOLER_POSTS WHERE (DATE(CREATED)=CATNAME) "
				+ "GROUP BY AUTHOR_ID ORDER BY PCNT DESC LIMIT 1) AS MN, COUNT(DISTINCT(AUTHOR_ID)) AS "
				+ "MX, COUNT(*) AS CNT FROM common.COOLER_POSTS WHERE (CREATED >= DATE_SUB(CURDATE(), "
				+ "INTERVAL ? DAY)) AND (CREATED <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY "
				+ "CATNAME ORDER BY CATNAME"); 
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, startDays);
			_ps.setInt(2, endDays);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns ACARS Flight Report performance metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getACARSFlights(int startDays, int endDays) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(FLIGHT_TIME) AS AV, MIN(FLIGHT_TIME) AS MN, MAX(FLIGHT_TIME) AS "
				+ "MX, COUNT(*) AS CNT FROM PIREPS WHERE (STATUS=?) AND (DATE >= DATE_SUB(CURDATE(), "
				+ "INTERVAL ? DAY)) AND (DATE <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND ((ATTR & ?) > 0) "
				+ "GROUP BY CATNAME ORDER BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, OK);
			_ps.setInt(2, startDays);
			_ps.setInt(3, endDays);
			_ps.setInt(4, ATTR_ACARS);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Flight Report performance metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getFlights(int startDays, int endDays) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(FLIGHT_TIME) AS AV, MIN(FLIGHT_TIME) AS MN, MAX(FLIGHT_TIME) AS "
				+ "MX, COUNT(*) AS CNT FROM PIREPS WHERE (STATUS=?) AND (DATE >= DATE_SUB(CURDATE(), "
				+ "INTERVAL ? DAY)) AND (DATE <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY CATNAME "
				+ "ORDER BY CATNAME");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, OK);
			_ps.setInt(2, startDays);
			_ps.setInt(3, endDays);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<PerformanceMetrics> execute() throws SQLException {
		List<PerformanceMetrics> results = new ArrayList<PerformanceMetrics>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			PerformanceMetrics pm = new PerformanceMetrics(rs.getString(1));
			pm.setAverage(rs.getDouble(2));
			pm.setLimits(rs.getDouble(3), rs.getDouble(4));
			pm.setCount(rs.getLong(5));
			results.add(pm);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
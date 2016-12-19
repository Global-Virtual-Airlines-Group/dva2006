// Copyright 2006, 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import static org.deltava.beans.flight.FlightReport.*;

import org.deltava.beans.testing.TestStatus;
import org.deltava.beans.stats.PerformanceMetrics;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load performance data from the database.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class GetPerformance extends DAO {
	
	private int _userID;
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
	 * @param sql the SQL statement
	 */
	public void setCategorySQL(String sql) {
		_categorySQL = sql;
	}
	
	/**
	 * Sets the user ID to monitor.
	 * @param id the user's database ID
	 */
	public void setUserID(int id) {
		_userID = id; 
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
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS AV, MIN(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS MN, "
			+ "MAX(TIME_TO_SEC(TIMEDIFF(DISPOSED, SUBMITTED)) / 3600) AS MX, COUNT(*) AS CNT FROM PIREPS WHERE (STATUS=?) AND (DATE >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) "
			+ "AND (DATE <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (SUBMITTED IS NOT NULL) ");
		if (_userID > 0)
			sqlBuf.append("AND (DISPOSAL_ID=?) ");
		sqlBuf.append("GROUP BY CATNAME ORDER BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, OK);
			_ps.setInt(2, startDays);
			_ps.setInt(3, endDays);
			if (_userID > 0)
				_ps.setInt(4, _userID);
			
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
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS AV, MIN(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS MN, "
			+ "MAX(TIME_TO_SEC(TIMEDIFF(E.GRADED_ON, E.SUBMITTED_ON)) / 3600) AS MX, COUNT(E.ID) AS CNT FROM exams.EXAMS E, exams.EXAMINFO EP WHERE (E.NAME=EP.NAME) AND "
			+ "(E.STATUS=?) AND (EP.AIRLINE=?) AND (E.CREATED_ON >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (E.CREATED_ON <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) ");
		if (_userID > 0)
			sqlBuf.append("AND (GRADED_BY=?) ");
		sqlBuf.append("GROUP BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, TestStatus.SCORED.ordinal());
			_ps.setString(2, SystemData.get("airline.code"));
			_ps.setInt(3, startDays);
			_ps.setInt(4, endDays);
			if (_userID > 0)
				_ps.setInt(5, _userID);

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
		sqlBuf.append(" AS CATNAME, AVG(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS AV, MIN(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS MN, "
			+ "MAX(TIME_TO_SEC(TIMEDIFF(CR.GRADED, CR.SUBMITTED)) / 3600) AS MX, COUNT(CR.ID) AS CNT FROM exams.CHECKRIDES CR, common.EQPROGRAMS EP WHERE (CR.STATUS=?) "
			+ "AND (CR.EQTYPE=EP.EQTYPE) AND (EP.OWNER=?) AND (CR.CREATED >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (CR.CREATED <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) "
			+ "AND (LOCATE(?, CR.NAME) = 0) ");
		if (_userID > 0)
			sqlBuf.append("AND (GRADED_BY=?) ");
		
		sqlBuf.append("GROUP BY CATNAME");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, TestStatus.SCORED.ordinal());
			_ps.setString(2, SystemData.get("airline.code"));
			_ps.setInt(3, startDays);
			_ps.setInt(4, endDays);
			_ps.setString(5, "Initial Hire");
			if (_userID > 0)
				_ps.setInt(6, _userID);

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns ACARS Flight Report performance metrics.
	 * @param startDays the number of days in the past to start, inclusive
	 * @param endDays the number of days in the past to end, inclusive
	 * @param isACARS TRUE if restricted to ACARS flights, otherwise FALSE
	 * @return a List of PerformanceMetrics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PerformanceMetrics> getFlights(int startDays, int endDays, boolean isACARS) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(_categorySQL);
		sqlBuf.append(" AS CATNAME, AVG(FLIGHT_TIME) AS AV, MIN(FLIGHT_TIME) AS MN, MAX(FLIGHT_TIME) AS MX, COUNT(*) AS CNT FROM PIREPS WHERE (STATUS=?) AND "
			+ "(DATE >= DATE_SUB(CURDATE(), INTERVAL ? DAY)) AND (DATE <= DATE_SUB(CURDATE(), INTERVAL ? DAY)) ");
		if (isACARS)
			sqlBuf.append("AND ((ATTR & ?) > 0) ");
		if (_userID > 0)
			sqlBuf.append("AND (DISPOSAL_ID=?)" );
		sqlBuf.append("GROUP BY CATNAME ORDER BY CATNAME");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString()); int pos = 0;
			_ps.setInt(++pos, OK);
			_ps.setInt(++pos, startDays);
			_ps.setInt(++pos, endDays);
			if (isACARS)
			_ps.setInt(++pos, ATTR_ACARS);
			if (_userID > 0)
				_ps.setInt(++pos, _userID);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private List<PerformanceMetrics> execute() throws SQLException {
		List<PerformanceMetrics> results = new ArrayList<PerformanceMetrics>();
		boolean isPilotID = isPilotID();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				PerformanceMetrics pm = new PerformanceMetrics(rs.getString(1));
				pm.setAverage(rs.getDouble(2));
				pm.setLimits(rs.getDouble(3), rs.getDouble(4));
				pm.setCount(rs.getLong(5));
				if (isPilotID) {
					int id = StringUtils.parse(pm.getName(), 0);
					if (id > 0)
						pm.setAuthorID(id);
				}
				
				results.add(pm);
			}
		}
		
		_ps.close();
		return results;
	}
}
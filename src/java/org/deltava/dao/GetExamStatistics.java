// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Examination and Check Ride statistics.
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class GetExamStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamStatistics(Connection c) {
		super(c);
	}

	/**
	 * Returns pilot examination statistics.
	 * @param label the label SQL
	 * @param subLabel the sub-label SQL
	 * @return a Collection of ExamStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamStatsEntry> getExamStatistics(String label, String subLabel) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(label);
		sqlBuf.append("AS LBL, YEAR(E.GRADED_ON)*100+MONTH(E.GRADED_ON) as DT, ");
		sqlBuf.append(subLabel);
		sqlBuf.append("AS SUBLBL, SUM(E.PASS) AS PS, COUNT(E.ID) AS CNT FROM EXAMS E,  EXAMINFO EI LEFT JOIN ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P ON (P.ID=E.GRADED_BY) WHERE (E.NAME=EI.NAME) AND (EI.AIRLINE=?) AND "
			+ "(E.ISEMPTY=?) AND (E.STATUS=?) GROUP BY LBL, SUBLBL");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, SystemData.get("airline.code"));
			_ps.setBoolean(2, false);
			_ps.setInt(3, Test.SCORED);
			
			// Execute the query
			Collection<ExamStatsEntry> results = new ArrayList<ExamStatsEntry>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamStatsEntry entry = new ExamStatsEntry(rs.getString(1));
				entry.setSubLabel(rs.getString(3));
				entry.setPassed(rs.getInt(4));
				entry.setTotal(rs.getInt(5));
				results.add(entry);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns check ride statistics.
	 * @param label the label SQL
	 * @param subLabel the sub-label SQL
	 * @param academyOnly TRUE if only Flight Academy check rides are included, otherwise FALSE 
	 * @return a Collection of ExamStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamStatsEntry> getCheckrideStatistics(String label, String subLabel, boolean academyOnly) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(label);
		sqlBuf.append("AS LBL, YEAR(C.GRADED)*100+MONTH(C.GRADED) as DT, ");
		sqlBuf.append(subLabel);
		sqlBuf.append("AS SUBLBL, SUM(C.PASS) AS PS, COUNT(C.ID) AS CNT FROM CHECKRIDES C, ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P WHERE (P.ID=C.GRADED_BY) AND (C.CREATED<>C.GRADED) AND (C.STATUS=?) ");
		if (academyOnly)
			sqlBuf.append("AND (C.ACADEMY=?) ");
		sqlBuf.append("GROUP BY LBL, SUBLBL");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, Test.SCORED);
			if (academyOnly)
				_ps.setBoolean(2, true);
			
			// Execute the query
			Collection<ExamStatsEntry> results = new ArrayList<ExamStatsEntry>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamStatsEntry entry = new ExamStatsEntry(rs.getString(1));
				entry.setSubLabel(rs.getString(3));
				entry.setPassed(rs.getInt(4));
				entry.setTotal(rs.getInt(5));
				results.add(entry);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
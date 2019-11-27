// Copyright 2010, 2011, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Examination and Check Ride statistics.
 * @author Luke
 * @version 9.0
 * @since 3.0
 */

public class GetExamStatistics extends DAO {
	
	private static final Cache<CacheableCollection<Integer>> _cache = new ExpiringCache<CacheableCollection<Integer>>(3, 3600);
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamStatistics(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the datbase IDs of all individuals in the current airline who scored an check ride.
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getCheckRideScorerIDs() throws DAOException {
		
		// Check the cache
		CacheableCollection<Integer> results = _cache.get(CheckRide.class);
		if (results != null)
			return results.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT C.GRADED_BY FROM exams.CHECKRIDES C, ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P WHERE (C.GRADED_BY=P.ID) AND (C.STATUS=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, TestStatus.SCORED.ordinal());
			
			// Execute the query
			results = new CacheableSet<Integer>(CheckRide.class);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the datbase IDs of all individuals in the current airline who scored an examination.
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getExamScorerIDs() throws DAOException {
		
		// Check the cache
		CacheableCollection<Integer> results = _cache.get(Examination.class);
		if (results != null)
			return results.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT E.GRADED_BY FROM exams.EXAMS E, ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P WHERE (E.GRADED_BY=P.ID) AND (E.STATUS=?) AND (E.AUTOSCORE=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, TestStatus.SCORED.ordinal());
			ps.setBoolean(2, false);
			
			// Execute the query
			results = new CacheableSet<Integer>(Examination.class);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns pilot examination statistics.
	 * @param label the label SQL
	 * @param subLabel the sub-label SQL
	 * @return a Collection of ExamStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamStatsEntry> getExamStatistics(String label, String subLabel) throws DAOException {
		return getExamStatistics(label, subLabel, 0);
	}

	/**
	 * Returns pilot examination statistics.
	 * @param label the label SQL
	 * @param subLabel the sub-label SQL
	 * @param scorerID the examination scorer's database ID, or zero
	 * @return a Collection of ExamStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamStatsEntry> getExamStatistics(String label, String subLabel, int scorerID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(label);
		sqlBuf.append("AS LBL, YEAR(C.GRADED_ON)*100+MONTH(C.GRADED_ON) as DT, ");
		sqlBuf.append(subLabel);
		sqlBuf.append("AS SUBLBL, SUM(C.PASS) AS PS, COUNT(C.ID) AS CNT, COUNT(DISTINCT C.PILOT_ID) FROM exams.EXAMS C, "
				+ "exams.EXAMINFO EI LEFT JOIN ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P ON (P.ID=C.GRADED_BY) WHERE (C.NAME=EI.NAME) AND (EI.AIRLINE=?) AND (C.ISEMPTY=?) AND (C.STATUS=?) ");
		if (scorerID > 0)
			sqlBuf.append("AND (C.GRADED_BY=?)");
		sqlBuf.append("GROUP BY LBL, SUBLBL");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, SystemData.get("airline.code"));
			ps.setBoolean(2, false);
			ps.setInt(3, TestStatus.SCORED.ordinal());
			if (scorerID > 0)
				ps.setInt(4, scorerID);
			
			// Execute the query
			Collection<ExamStatsEntry> results = new ArrayList<ExamStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ExamStatsEntry entry = new ExamStatsEntry(rs.getString(1));
					entry.setSubLabel(rs.getString(3));
					entry.setPassed(rs.getInt(4));
					entry.setTotal(rs.getInt(5));
					entry.setUsers(rs.getInt(6));
					results.add(entry);
				}
			}
			
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
		return getCheckrideStatistics(label, subLabel, academyOnly, 0, null);
	}
	
	/**
	 * Returns check ride statistics.
	 * @param label the label SQL
	 * @param subLabel the sub-label SQL
	 * @param academyOnly TRUE if only Flight Academy check rides are included, otherwise FALSE 
	 * @param scorerID the check ride scorer's database ID, or zero
	 * @param eqProgram an Equipment Type program, or null for all
	 * @return a Collection of ExamStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamStatsEntry> getCheckrideStatistics(String label, String subLabel, boolean academyOnly, int scorerID, String eqProgram) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(label);
		sqlBuf.append("AS LBL, YEAR(C.GRADED)*100+MONTH(C.GRADED) as DT, ");
		sqlBuf.append(subLabel);
		sqlBuf.append("AS SUBLBL, SUM(C.PASS) AS PS, COUNT(C.ID) AS CNT, COUNT(DISTINCT C.PILOT_ID) FROM exams.CHECKRIDES C, ");
		sqlBuf.append(SystemData.get("airline.db"));
		sqlBuf.append(".PILOTS P WHERE (P.ID=C.GRADED_BY) AND (C.CREATED<>C.GRADED) AND (C.STATUS=?) ");
		if (academyOnly)
			sqlBuf.append("AND (C.ACADEMY=?) ");
		if (scorerID > 0)
			sqlBuf.append("AND (C.GRADED_BY=?) ");
		if (eqProgram != null)
			sqlBuf.append("AND (C.EQTYPE=?) ");
		sqlBuf.append("GROUP BY LBL, SUBLBL ORDER BY ");
		sqlBuf.append(label.startsWith("DATE") ? "DT DESC" : "LBL");
		sqlBuf.append(", ");
		sqlBuf.append(subLabel.startsWith("DATE") ? "DT DESC" : "LBL");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int pos = 0;
			ps.setInt(++pos, TestStatus.SCORED.ordinal());
			if (academyOnly)
				ps.setBoolean(++pos, true);
			if (scorerID > 0)
				ps.setInt(++pos, scorerID);
			if (eqProgram != null)
				ps.setString(++pos, eqProgram);
			
			// Execute the query
			Collection<ExamStatsEntry> results = new ArrayList<ExamStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ExamStatsEntry entry = new ExamStatsEntry(rs.getString(1));
					entry.setSubLabel(rs.getString(3));
					entry.setPassed(rs.getInt(4));
					entry.setTotal(rs.getInt(5));
					entry.setUsers(rs.getInt(6));
					results.add(entry);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
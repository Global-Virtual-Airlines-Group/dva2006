// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read examination configuration data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class GetExamProfiles extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamProfiles(Connection c) {
		super(c);
	}

	/**
	 * Loads an Examination profile.
	 * @param examName the examination name
	 * @return an ExamProfile bean, or null if the exam was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ExamProfile getExamProfile(String examName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM exams.EXAMINFO WHERE (NAME=?) LIMIT 1");
			_ps.setString(1, examName);

			// Execute the query - return null if not found
			List<ExamProfile> results = execute();
			loadAirlines(results);
			loadScorers(results);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Examination Profiles.
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getAllExamProfiles() throws DAOException {
		try {
			prepareStatement("SELECT * FROM exams.EXAMINFO ORDER BY STAGE, NAME");
			List<ExamProfile> results = execute();
			loadAirlines(results);
			loadScorers(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Examination profiles for the current Airline.
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getExamProfiles() throws DAOException {
		try {
			prepareStatement("SELECT E.* FROM exams.EXAMINFO E, exams.EXAM_AIRLINES EA WHERE (E.NAME=EA.NAME) "
					+ "AND (EA.AIRLINE=?) ORDER BY E.STAGE, E.NAME");
			_ps.setString(1, SystemData.get("airline.code"));
			List<ExamProfile> results = execute();
			loadAirlines(results);
			loadScorers(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Examination profiles included within the Flight Academy or Testing Center.
	 * @param isAcademy TRUE if Flight Academy exams should be returned, otherwise FALSE for the Testing Center
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getExamProfiles(boolean isAcademy) throws DAOException {
		try {
			prepareStatement("SELECT E.* FROM exams.EXAMINFO E, exams.EXAM_AIRLINES EA WHERE (E.NAME=EA.NAME) "
					+ "AND (E.ACADEMY=?) AND (EA.AIRLINE=?) ORDER BY E.STAGE, E.NAME");
			_ps.setBoolean(1, isAcademy);
			_ps.setString(2, SystemData.get("airline.code"));
			List<ExamProfile> results = execute();
			loadAirlines(results);
			loadScorers(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the sub-pools for a particular Examination.
	 * @param examName the Examination name
	 * @return a List of ExamSubPool beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamSubPool> getSubPools(String examName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ESP.ID, ESP.SUBPOOL, IF(ESP.SIZE=0, E.QUESTIONS, ESP.SIZE) "
					+ "AS QNUM, COUNT(QE.QUESTION_ID) FROM exams.EXAMINFO E LEFT JOIN exams.EXAM_QPOOLS ESP "
					+ "ON (E.NAME=ESP.NAME) LEFT JOIN exams.QE_INFO QE ON (ESP.ID=QE.SUBPOOL_ID) AND "
					+ "(ESP.NAME=QE.EXAM_NAME) WHERE (E.NAME=?) GROUP BY ESP.ID");
			_ps.setString(1, examName);
			
			// Execute the query
			List<ExamSubPool> results = new ArrayList<ExamSubPool>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamSubPool sp = new ExamSubPool(examName, rs.getString(2));
				sp.setID(rs.getInt(1));
				sp.setSize(rs.getInt(3));
				sp.setPoolSize(rs.getInt(4));
				results.add(sp);
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
	 * Returns Examination sub-pools for the current Airline.
	 * @return a Collection of ExamSubPool beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamSubPool> getSubPools() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT E.NAME, ESP.SUBPOOL, ESP.ID, IF(ESP.SIZE=0, E.QUESTIONS, ESP.SIZE) AS QNUM, "
					+ "COUNT(QE.QUESTION_ID) FROM exams.EXAM_AIRLINE EA, exams.EXAMINFO E LEFT JOIN exams.EXAM_QPOOLS ESP "
					+ "ON (E.NAME=ESP.NAME) LEFT JOIN exams.QE_INFO QE ON (ESP.NAME=QE.EXAM_NAME) AND (ESP.ID=QE.SUBPOOL_ID) "
					+ "WHERE (E.NAME=EA.NAME) AND (EA.AIRLINE=?) GROUP BY E.NAME, ESP.ID ORDER BY E.STAGE, E.NAME, ESP.ID");
			_ps.setString(1, SystemData.get("airline.code"));

			// Execute the Query
			List<ExamSubPool> results = new ArrayList<ExamSubPool>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamSubPool sp = new ExamSubPool(rs.getString(1), rs.getString(2));
				sp.setID(rs.getInt(3));
				sp.setSize(rs.getInt(4));
				sp.setPoolSize(rs.getInt(5));
				results.add(sp);
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
	 * Returns Examination sub-pools for all Airlines.
	 * @return a Collection of ExamSubPool beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ExamSubPool> getAllSubPools() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT E.NAME, ESP.SUBPOOL, ESP.ID, IF(ESP.SIZE=0, E.QUESTIONS, ESP.SIZE) AS QNUM, "
					+ "COUNT(QE.QUESTION_ID) FROM exams.EXAMINFO E LEFT JOIN exams.EXAM_QPOOLS ESP ON (E.NAME=ESP.NAME) "
					+ "LEFT JOIN exams.QE_INFO QE ON (ESP.NAME=QE.EXAM_NAME) AND (ESP.ID=QE.SUBPOOL_ID) GROUP BY E.NAME, "
					+ "ESP.ID ORDER BY E.STAGE, E.NAME, ESP.ID");
			
			// Execute the Query
			List<ExamSubPool> results = new ArrayList<ExamSubPool>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamSubPool sp = new ExamSubPool(rs.getString(1), rs.getString(2));
				sp.setID(rs.getInt(3));
				sp.setSize(rs.getInt(4));
				sp.setPoolSize(rs.getInt(5));
				results.add(sp);
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
	 * Loads a Check Ride script.
	 * @param eqType the equipment type
	 * @return a CheckRideScript bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRideScript getScript(String eqType) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM CR_DESCS WHERE (EQTYPE=?) LIMIT 1");
			_ps.setString(1, eqType);

			// Execute the Query - return null if nothing found
			CheckRideScript result = null;
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				result = new CheckRideScript(rs.getString(1));
				result.setProgram(rs.getString(2));
				result.setDescription(rs.getString(3));
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Check Ride scripts.
	 * @return a List of CheckRideScript beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRideScript> getScripts() throws DAOException {
		try {
			prepareStatement("SELECT * FROM CR_DESCS");

			// Execute the query
			List<CheckRideScript> results = new ArrayList<CheckRideScript>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				CheckRideScript sc = new CheckRideScript(rs.getString(1));
				sc.setProgram(rs.getString(2));
				sc.setDescription(rs.getString(3));
				results.add(sc);
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
	 * Helper method to parse ExamProfile result sets.
	 */
	private List<ExamProfile> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<ExamProfile> results = new ArrayList<ExamProfile>();
		while (rs.next()) {
			ExamProfile ep = new ExamProfile(rs.getString(1));
			ep.setStage(rs.getInt(2));
			ep.setMinStage(rs.getInt(3));
			ep.setEquipmentType(rs.getString(4));
			ep.setSize(rs.getInt(5));
			ep.setPassScore(rs.getInt(6));
			ep.setTime(rs.getInt(7));
			ep.setActive(rs.getBoolean(8));
			ep.setAcademy(rs.getBoolean(9));
			ep.setOwner(SystemData.getApp(rs.getString(10)));
			results.add(ep);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to load airlines for Exams.
	 */
	private void loadAirlines(Collection<ExamProfile> eProfiles) throws SQLException {
		Map<String, ExamProfile> exams = CollectionUtils.createMap(eProfiles, "name");
		if (eProfiles.isEmpty())
			return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT NAME, AIRLINE FROM exams.EXAM_AIRLINES WHERE NAME IN (");
		for (Iterator<ExamProfile> i = eProfiles.iterator(); i.hasNext();) {
			ExamProfile ep = i.next();
			sqlBuf.append('\'');
			sqlBuf.append(ep.getName());
			sqlBuf.append('\'');
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append(')');

		// Execute the query
		prepareStatementWithoutLimits(sqlBuf.toString());
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			ExamProfile ep = exams.get(rs.getString(1));
			if (ep != null)
				ep.addAirline(SystemData.getApp(rs.getString(2)));
		}

		// Clean up
		rs.close();
		_ps.close();
	}
	
	/**
	 * Helper method to load scorers for Exams.
	 */
	private void loadScorers(Collection<ExamProfile> eProfiles) throws SQLException {
		Map<String, ExamProfile> exams = CollectionUtils.createMap(eProfiles, "name");
		if (eProfiles.isEmpty())
			return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT NAME, PILOT_ID FROM exams.EXAMSCORERS WHERE NAME IN (");
		for (Iterator<ExamProfile> i = eProfiles.iterator(); i.hasNext();) {
			ExamProfile ep = i.next();
			sqlBuf.append('\'');
			sqlBuf.append(ep.getName());
			sqlBuf.append('\'');
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(')');
		
		// Execute the query
		prepareStatementWithoutLimits(sqlBuf.toString());
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			ExamProfile ep = exams.get(rs.getString(1));
			if (ep != null)
				ep.addScorerID(rs.getInt(2));
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}
}
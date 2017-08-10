// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read examination configuration data.
 * @author Luke
 * @version 7.5
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
	 * Loads a Check Ride script.
	 * @param eqType the equipment type
	 * @return a CheckRideScript bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EquipmentRideScript getScript(String eqType) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM CR_DESCS WHERE (EQTYPE=?) LIMIT 1");
			_ps.setString(1, eqType);

			// Execute the Query - return null if nothing found
			EquipmentRideScript result = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					result = new EquipmentRideScript(rs.getString(1));
					result.setProgram(rs.getString(2));
					result.setDescription(rs.getString(3));
				}
			}

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
	public List<EquipmentRideScript> getScripts() throws DAOException {
		try {
			prepareStatement("SELECT * FROM CR_DESCS");

			// Execute the query
			List<EquipmentRideScript> results = new ArrayList<EquipmentRideScript>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					EquipmentRideScript sc = new EquipmentRideScript(rs.getString(1));
					sc.setProgram(rs.getString(2));
					sc.setDescription(rs.getString(3));
					results.add(sc);
				}
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all available equipment/aircraft script combinations. 
	 * @return a Map of Collections of aircraft names, keyed by equipment profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Collection<String>> getAircraftScripts() throws DAOException {
		try {
			Map<String, Collection<String>> results = new LinkedHashMap<String, Collection<String>>();
			prepareStatementWithoutLimits("SELECT EQPROGRAM, EQTYPE FROM CR_DESCS");
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					CollectionUtils.addMapCollection(results, rs.getString(1), rs.getString(2));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse ExamProfile result sets.
	 */
	private List<ExamProfile> execute() throws SQLException {
		List<ExamProfile> results = new ArrayList<ExamProfile>();
		try (ResultSet rs = _ps.executeQuery()) {
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
				ep.setNotify(rs.getBoolean(10));
				ep.setOwner(SystemData.getApp(rs.getString(11)));
				results.add(ep);
			}
		}

		_ps.close();
		return results;
	}

	/*
	 * Helper method to load airlines for Exams.
	 */
	private void loadAirlines(Collection<ExamProfile> eProfiles) throws SQLException {
		Map<String, ExamProfile> exams = CollectionUtils.createMap(eProfiles, ExamProfile::getName);
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
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				ExamProfile ep = exams.get(rs.getString(1));
				if (ep != null)
					ep.addAirline(SystemData.getApp(rs.getString(2)));
			}
		}

		_ps.close();
	}
	
	/*
	 * Helper method to load scorers for Exams.
	 */
	private void loadScorers(Collection<ExamProfile> eProfiles) throws SQLException {
		Map<String, ExamProfile> exams = CollectionUtils.createMap(eProfiles, ExamProfile::getName);
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
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				ExamProfile ep = exams.get(rs.getString(1));
				if (ep != null)
					ep.addScorerID(rs.getInt(2));
			}
		}
		
		_ps.close();
	}
}
// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Simulator;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read examination configuration data.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class GetExamProfiles extends DAO {
	
	private static final Cache<CacheableCollection<ExamProfile>> _cache = CacheManager.getCollection(ExamProfile.class, "ExamProfiles");

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamProfiles(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all Examination Profiles.
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getAllExamProfiles() throws DAOException {
		
		// Check the cache
		CacheableCollection<ExamProfile> results = _cache.get(ExamProfile.class);
		if (results != null)
			return new ArrayList<ExamProfile>(results);
		
		results = new CacheableList<ExamProfile>(ExamProfile.class);
		try (PreparedStatement ps = prepare("SELECT EI.*, COUNT(E.ID), SUM(E.PASS), COUNT(QE.QUESTION_ID) FROM exams.EXAMINFO EI LEFT JOIN exams.EXAMS E ON (EI.NAME=E.NAME) LEFT JOIN "
			+ "exams.QE_INFO QE ON (EI.NAME=QE.EXAM_NAME) GROUP BY EI.NAME ORDER BY EI.STAGE, EI.NAME")) {
			results.addAll(execute(ps));
			loadAirlines(results);
			loadScorers(results);
			_cache.add(results);
			return new ArrayList<ExamProfile>(results);
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
		AirlineInformation ai = SystemData.getApp(null);
		return getAllExamProfiles().stream().filter(ep -> ep.getAirlines().contains(ai)).collect(Collectors.toList());
	}
	
	/**
	 * Loads an Examination profile.
	 * @param examName the examination name
	 * @return an ExamProfile bean, or null if the exam was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ExamProfile getExamProfile(String examName) throws DAOException {
		return getAllExamProfiles().stream().filter(ep -> ep.getName().equals(examName)).findFirst().orElse(null);
	}

	/**
	 * Returns all Examination profiles included within the Flight Academy or Testing Center.
	 * @param isAcademy TRUE if Flight Academy exams should be returned, otherwise FALSE for the Testing Center
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getExamProfiles(boolean isAcademy) throws DAOException {
		return getExamProfiles().stream().filter(ep -> (ep.getAcademy() == isAcademy)).collect(Collectors.toList());
	}
	
	/**
	 * Loads a Check Ride script.
	 * @param key the script key
	 * @return an EquipmentRideScript bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EquipmentRideScript getScript(EquipmentRideScriptKey key) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CD.*, GROUP_CONCAT(CS.SIM SEPARATOR ?) FROM CR_DESCS CD LEFT JOIN CR_SIMS CS ON ((CD.EQPROGRAM=CS.EQPROGRAM) "
			+ "AND (CD.EQTYPE=CS.EQTYPE) AND (CD.CURRENCY=CS.CURRENCY)) WHERE (CD.EQPROGRAM=?) AND (CD.EQTYPE=?) AND (CD.CURRENCY=?) GROUP BY CD.EQPROGRAM, CD.EQTYPE, CD.CURRENCY")) {
			ps.setString(1, ",");
			ps.setString(2, key.getProgramName());
			ps.setString(3, key.getEquipmentType());
			ps.setBoolean(4, key.isCurrency());
			EquipmentRideScript result = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					result = new EquipmentRideScript(rs.getString(2), rs.getString(1));
					result.setIsCurrency(rs.getBoolean(3));
					result.setIsDefault(rs.getBoolean(4));
					result.setDescription(rs.getString(5));
					List<String> sims = StringUtils.split(rs.getString(6), ",");
					if (sims != null)
						sims.stream().map(c -> Simulator.fromName(c, Simulator.UNKNOWN)).filter(s -> (s != Simulator.UNKNOWN)).forEach(result::addSimulator);
				}
			}

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
		try (PreparedStatement ps = prepare("SELECT CD.*, GROUP_CONCAT(CS.SIM SEPARATOR ?) FROM CR_DESCS CD LEFT JOIN CR_SIMS CS ON ((CD.EQPROGRAM=CS.EQPROGRAM) AND "
			+ "(CD.EQTYPE=CS.EQTYPE) AND (CD.CURRENCY=CS.CURRENCY)) GROUP BY CD.EQPROGRAM, CD.EQTYPE, CD.CURRENCY")) {
			ps.setString(1, ",");
			List<EquipmentRideScript> results = new ArrayList<EquipmentRideScript>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					EquipmentRideScript sc = new EquipmentRideScript(rs.getString(2), rs.getString(1));
					sc.setIsCurrency(rs.getBoolean(3));
					sc.setIsDefault(rs.getBoolean(4));
					sc.setDescription(rs.getString(5));
					List<String> sims = StringUtils.split(rs.getString(6), ",");
					if (sims != null)
						sims.stream().map(c -> Simulator.fromName(c, Simulator.UNKNOWN)).filter(s -> (s != Simulator.UNKNOWN)).forEach(sc::addSimulator);
					
					results.add(sc);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse ExamProfile result sets.
	 */
	private static List<ExamProfile> execute(PreparedStatement ps) throws SQLException {
		List<ExamProfile> results = new ArrayList<ExamProfile>();
		try (ResultSet rs = ps.executeQuery()) {
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
				ep.setTotal(rs.getInt(12));
				ep.setPassCount(rs.getInt(13));
				ep.setQuestionPoolSize(rs.getInt(14));
				results.add(ep);
			}
		}

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
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ExamProfile ep = exams.get(rs.getString(1));
					if (ep != null)
						ep.addAirline(SystemData.getApp(rs.getString(2)));
				}
			}
		}
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
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ExamProfile ep = exams.get(rs.getString(1));
					if (ep != null)
						ep.addScorerID(rs.getInt(2));
				}
			}
		}
	}
}
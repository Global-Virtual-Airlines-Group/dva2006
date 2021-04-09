// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;
import org.deltava.beans.Simulator;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object for writing Examination Profiles and Check Ride scripts.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SetExamProfile extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetExamProfile(Connection c) {
		super(c);
	}

	/**
	 * Saves an existing Examination Profile to the database.
	 * @param ep the ExamProfile bean to update
	 * @param examName the old Examination Profile name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(ExamProfile ep, String examName) throws DAOException {
		try {
			startTransaction();

			// Clean out the airlines
			try (PreparedStatement ps = prepareWithoutLimits("DELETE from exams.EXAM_AIRLINES WHERE (NAME=?)")) {
				ps.setString(1, examName);
				executeUpdate(ps, 0);
			}
			
			// Clean out the scorers
			try (PreparedStatement ps = prepareWithoutLimits("DELETE from exams.EXAMSCORERS WHERE (NAME=?)")) {
				ps.setString(1, examName);
				executeUpdate(ps, 0);
			}

			// Write the profile
			try (PreparedStatement ps = prepare("UPDATE exams.EXAMINFO SET STAGE=?, QUESTIONS=?, PASS_SCORE=?, TIME=?, ACTIVE=?, EQTYPE=?, MIN_STAGE=?, ACADEMY=?, NOTIFY=?, NAME=?, AIRLINE=? WHERE (NAME=?)")) {
				ps.setInt(1, ep.getStage());
				ps.setInt(2, ep.getSize());
				ps.setInt(3, ep.getPassScore());
				ps.setInt(4, ep.getTime());
				ps.setBoolean(5, ep.getActive());
				ps.setString(6, ep.getEquipmentType());
				ps.setInt(7, ep.getMinStage());
				ps.setBoolean(8, ep.getAcademy());
				ps.setBoolean(9, ep.getNotify());
				ps.setString(10, ep.getName());
				ps.setString(11, ep.getOwner().getCode());
				ps.setString(12, examName);
				executeUpdate(ps, 1);
			}

			// Write the new airlines
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)")) {
				ps.setString(1, ep.getName());
				for (AirlineInformation ai : ep.getAirlines()) {
					ps.setString(2, ai.getCode());
					ps.addBatch();
				}

				executeUpdate(ps, 1, ep.getAirlines().size());
			}
			
			// Write the new scorers 
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMSCORERS (NAME, PILOT_ID) VALUES (?, ?)")) {
				ps.setString(1, ep.getName());
				for (Integer id : ep.getScorerIDs()) {
					ps.setInt(2, id.intValue());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, ep.getScorerIDs().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("ExamProfiles", ExamProfile.class);
		}
	}

	/**
	 * Saves a new Examination Profile to the database.
	 * @param ep the ExamProfile bean to save
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(ExamProfile ep) throws DAOException {
		try {
			startTransaction();

			// Write the exam profile
			try (PreparedStatement ps = prepare("INSERT INTO exams.EXAMINFO (NAME, STAGE, QUESTIONS, PASS_SCORE, TIME, ACTIVE, EQTYPE, MIN_STAGE, ACADEMY, NOTIFY, AIRLINE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, ep.getName());
				ps.setInt(2, ep.getStage());
				ps.setInt(3, ep.getSize());
				ps.setInt(4, ep.getPassScore());
				ps.setInt(5, ep.getTime());
				ps.setBoolean(6, ep.getActive());
				ps.setString(7, ep.getEquipmentType());
				ps.setInt(8, ep.getMinStage());
				ps.setBoolean(9, ep.getAcademy());
				ps.setBoolean(10, ep.getNotify());
				ps.setString(11, ep.getOwner().getCode());
				executeUpdate(ps, 1);
			}
			
			// Write the airlines
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)")) {
				ps.setString(1, ep.getName());
				for (AirlineInformation ai : ep.getAirlines()) {
					ps.setString(2, ai.getCode());
					ps.addBatch();
				}

				executeUpdate(ps, 1, ep.getAirlines().size());
			}
			
			// Write the scorers
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMSCORERS (NAME, PILOT_ID) VALUES (?, ?)")) {
				ps.setString(1, ep.getName());
				for (Integer id : ep.getScorerIDs()) {
					ps.setInt(2, id.intValue());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, ep.getScorerIDs().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("ExamProfiles", ExamProfile.class);
		}
	}

	/**
	 * Writes a Check Ride script to the database. This call can handle both INSERT and UPDATE operations.
	 * @param sc the Check Ride script
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EquipmentRideScript sc) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM CR_SIMS WHERE (EQTYPE=?) AND (EQPROGRAM=?) AND (CURRENCY=?)")) {
				ps.setString(1, sc.getEquipmentType());
				ps.setString(2, sc.getProgram());
				ps.setBoolean(3, sc.getIsCurrency());
				executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO CR_DESCS (EQTYPE, EQPROGRAM, CURRENCY, ISDEFAULT, BODY) VALUES (?, ?, ?, ?, ?)")) {
				ps.setString(1, sc.getEquipmentType());
				ps.setString(2, sc.getProgram());
				ps.setBoolean(3, sc.getIsCurrency());
				ps.setBoolean(4, sc.getIsDefault());
				ps.setString(5, sc.getDescription());
				executeUpdate(ps, 1);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO CR_SIMS (EQTYPE, EQPROGRAM, CURRENCY, SIM) VALUES (?, ?, ?, ?)")) {
				ps.setString(1, sc.getEquipmentType());
				ps.setString(2, sc.getProgram());
				ps.setBoolean(3, sc.getIsCurrency());
				for (Simulator s : sc.getSimulators()) {
					ps.setString(4, s.name());
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, sc.getSimulators().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Check Ride Script from the database.
	 * @param key the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(EquipmentRideScriptKey key) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM CR_DESCS WHERE (EQTYPE=?) AND (CURRENCY=?)")) {
			ps.setString(1, key.getEquipmentType());
			ps.setBoolean(2, key.isCurrency());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Examination Profile from the database.
	 * @param ep an ExamProfile bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(ExamProfile ep) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM exams.EXAMINFO WHERE (NAME=?)")){
			ps.setString(1, ep.getName());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("ExamProfiles", ExamProfile.class);
		}
	}
}
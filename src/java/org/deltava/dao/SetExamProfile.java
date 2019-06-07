// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object for writing Examination Profiles and Check Ride scripts.
 * @author Luke
 * @version 8.6
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
			prepareStatementWithoutLimits("DELETE from exams.EXAM_AIRLINES WHERE (NAME=?)");
			_ps.setString(1, examName);
			executeUpdate(0);
			
			// Clean out the scorers
			prepareStatementWithoutLimits("DELETE from exams.EXAMSCORERS WHERE (NAME=?)");
			_ps.setString(1, examName);
			executeUpdate(0);

			// Write the profile
			prepareStatement("UPDATE exams.EXAMINFO SET STAGE=?, QUESTIONS=?, PASS_SCORE=?, TIME=?, ACTIVE=?, EQTYPE=?, MIN_STAGE=?, ACADEMY=?, NOTIFY=?, NAME=?, AIRLINE=? WHERE (NAME=?)");
			_ps.setInt(1, ep.getStage());
			_ps.setInt(2, ep.getSize());
			_ps.setInt(3, ep.getPassScore());
			_ps.setInt(4, ep.getTime());
			_ps.setBoolean(5, ep.getActive());
			_ps.setString(6, ep.getEquipmentType());
			_ps.setInt(7, ep.getMinStage());
			_ps.setBoolean(8, ep.getAcademy());
			_ps.setBoolean(9, ep.getNotify());
			_ps.setString(10, ep.getName());
			_ps.setString(11, ep.getOwner().getCode());
			_ps.setString(12, examName);
			executeUpdate(1);

			// Write the new airlines
			prepareStatementWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, ep.getName());
			for (AirlineInformation ai : ep.getAirlines()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			executeBatchUpdate(1, ep.getAirlines().size());
			
			// Write the new scorers 
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMSCORERS (NAME, PILOT_ID) VALUES (?, ?)");
			_ps.setString(1, ep.getName());
			for (Integer id : ep.getScorerIDs()) {
				_ps.setInt(2, id.intValue());
				_ps.addBatch();
			}
			
			executeBatchUpdate(1, ep.getScorerIDs().size());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
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
			prepareStatement("INSERT INTO exams.EXAMINFO (NAME, STAGE, QUESTIONS, PASS_SCORE, TIME, ACTIVE, "
					+ "EQTYPE, MIN_STAGE, ACADEMY, NOTIFY, AIRLINE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, ep.getName());
			_ps.setInt(2, ep.getStage());
			_ps.setInt(3, ep.getSize());
			_ps.setInt(4, ep.getPassScore());
			_ps.setInt(5, ep.getTime());
			_ps.setBoolean(6, ep.getActive());
			_ps.setString(7, ep.getEquipmentType());
			_ps.setInt(8, ep.getMinStage());
			_ps.setBoolean(9, ep.getAcademy());
			_ps.setBoolean(10, ep.getNotify());
			_ps.setString(11, ep.getOwner().getCode());
			executeUpdate(1);
			
			// Write the airlines
			prepareStatementWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, ep.getName());
			for (AirlineInformation ai : ep.getAirlines()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			executeBatchUpdate(1, ep.getAirlines().size());
			
			// Write the scorers
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMSCORERS (NAME, PILOT_ID) VALUES (?, ?)");
			_ps.setString(1, ep.getName());
			for (Integer id : ep.getScorerIDs()) {
				_ps.setInt(2, id.intValue());
				_ps.addBatch();
			}
			
			executeBatchUpdate(1, ep.getScorerIDs().size());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}


	/**
	 * Writes a Check Ride script to the database. This call can handle both INSERT and UPDATE operations.
	 * @param sc the Check Ride script
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EquipmentRideScript sc) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO CR_DESCS (EQTYPE, EQPROGRAM, CURRENCY, SIMS, BODY) VALUES (?, ?, ?, ?, ?)");
			_ps.setString(1, sc.getEquipmentType());
			_ps.setString(2, sc.getProgram());
			_ps.setBoolean(3, sc.getIsCurrency());
			_ps.setString(4, StringUtils.listConcat(sc.getSimulators(), ","));
			_ps.setString(5, sc.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Check Ride Script from the database.
	 * @param key the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(EquipmentRideScriptKey key) throws DAOException {
		try {
			prepareStatement("DELETE FROM CR_DESCS WHERE (EQTYPE=?) AND (CURRENCY=?)");
			_ps.setString(1, key.getEquipmentType());
			_ps.setBoolean(2, key.isCurrency());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object for writing Examination/Question Profiles and Check Ride scripts.
 * @author Luke
 * @version 1.0
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
			_ps.setString(1, ep.getName());
			executeUpdate(0);

			// Write the profile
			prepareStatement("UPDATE exams.EXAMINFO SET STAGE=?, QUESTIONS=?, PASS_SCORE=?, TIME=?, "
					+ "ACTIVE=?, EQTYPE=?, MIN_STAGE=?, ACADEMY=?, NAME=?, AIRLINE=? WHERE (NAME=?)");
			_ps.setInt(1, ep.getStage());
			_ps.setInt(2, ep.getSize());
			_ps.setInt(3, ep.getPassScore());
			_ps.setInt(4, ep.getTime());
			_ps.setBoolean(5, ep.getActive());
			_ps.setString(6, ep.getEquipmentType());
			_ps.setInt(7, ep.getMinStage());
			_ps.setBoolean(8, ep.getAcademy());
			_ps.setString(9, ep.getName());
			_ps.setString(10, ep.getOwner().getCode());
			_ps.setString(11, examName);
			executeUpdate(1);

			// Write the new airlines
			prepareStatementWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, examName);
			for (Iterator<AirlineInformation> i = ep.getAirlines().iterator(); i.hasNext();) {
				AirlineInformation ai = i.next();
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			// Execute the update and commit
			_ps.executeBatch();
			_ps.close();
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
					+ "EQTYPE, MIN_STAGE, ACADEMY, AIRLINE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, ep.getName());
			_ps.setInt(2, ep.getStage());
			_ps.setInt(3, ep.getSize());
			_ps.setInt(4, ep.getPassScore());
			_ps.setInt(5, ep.getTime());
			_ps.setBoolean(6, ep.getActive());
			_ps.setString(7, ep.getEquipmentType());
			_ps.setInt(8, ep.getMinStage());
			_ps.setBoolean(9, ep.getAcademy());
			_ps.setString(10, ep.getOwner().getCode());
			executeUpdate(1);

			// Write the airlines
			prepareStatementWithoutLimits("INSERT INTO exams.EXAM_AIRLINES (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, ep.getName());
			for (Iterator<AirlineInformation> i = ep.getAirlines().iterator(); i.hasNext();) {
				AirlineInformation ai = i.next();
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			// Execute the update and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Examination Question Profile to the database. This call can handle both INSERT and UPDATE operations.
	 * If an INSERT operation is performed, the auto-assigned database ID will be set in the bean.
	 * @param qp the QuestionProfile bean to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(QuestionProfile qp) throws DAOException {
		try {
			startTransaction();
			
			// Prepare different statements for INSERT and UPDATE operations
			if (qp.getID() == 0) {
				prepareStatement("INSERT INTO exams.QUESTIONINFO (QUESTION, CORRECT, ACTIVE, AIRLINE) VALUES (?, ?, ?, ?)");
				_ps.setString(4, qp.getOwner().getCode());
			} else {
				prepareStatementWithoutLimits("DELETE FROM exams.QUESTIONAIRLINES WHERE (ID=?)");
				_ps.setInt(1, qp.getID());
				executeUpdate(0);
				prepareStatement("UPDATE exams.QUESTIONINFO SET QUESTION=?, CORRECT=?, ACTIVE=? WHERE (ID=?)");
				_ps.setInt(4, qp.getID());
			}

			// Set prepared statement and write the question
			_ps.setString(1, qp.getQuestion());
			_ps.setString(2, qp.getCorrectAnswer());
			_ps.setBoolean(3, qp.getActive());
			executeUpdate(1);

			// If this is a new question profile, get the ID back from the database, otherwise clear the exam names
			if (qp.getID() == 0)
				qp.setID(getNewID());
			else {
				prepareStatementWithoutLimits("DELETE FROM exams.QE_INFO WHERE (QUESTION_ID=?)");
				_ps.setInt(1, qp.getID());
				executeUpdate(0);

				// Clear out the multiple choice options if we have them
				if (qp instanceof MultipleChoice) {
					prepareStatementWithoutLimits("DELETE FROM exams.QUESTIONMINFO WHERE (ID=?)");
					_ps.setInt(1, qp.getID());
					executeUpdate(0);
				}
			}

			// Write the exam names
			prepareStatementWithoutLimits("INSERT INTO exams.QE_INFO (QUESTION_ID, EXAM_NAME) VALUES (?, ?)");
			_ps.setInt(1, qp.getID());
			for (Iterator<String> i = qp.getExamNames().iterator(); i.hasNext();) {
				String examName = i.next();
				_ps.setString(2, examName);
				_ps.addBatch();
			}

			// Execute the batch statement and clean up
			_ps.executeBatch();
			_ps.close();
			
			// Write the airline names
			prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONAIRLINES (ID, AIRLINE) VALUES (?, ?)");
			_ps.setInt(1, qp.getID());
			for (Iterator<AirlineInformation> i = qp.getAirlines().iterator(); i.hasNext(); ) {
				AirlineInformation ai = i.next();
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			// Execute the batch statement and clean up
			_ps.executeBatch();
			_ps.close();

			// Write the multiple choice entries
			if (qp instanceof MultipleChoice) {
				MultipleChoice mq = (MultipleChoice) qp;
				prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)");
				_ps.setInt(1, qp.getID());

				// Write the entries
				int seq = 0;
				for (Iterator<String> i = mq.getChoices().iterator(); i.hasNext();) {
					String choice = i.next();
					_ps.setInt(2, ++seq);
					_ps.setString(3, choice);
					_ps.addBatch();
				}

				// Execute the batch transaction
				_ps.executeBatch();
				_ps.close();
			}

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a multiple-choice Examination Question Profile to the database. This call can handle both INSERT and
	 * UPDATE operations. If an INSERT operation is performed, the auto-assigned database ID will be set in the bean.
	 * @param mqp the QuestionProfile bean to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(MultiChoiceQuestionProfile mqp) throws DAOException {

		boolean isNew = (mqp.getID() == 0);
		try {
			startTransaction();
			write((QuestionProfile) mqp);

			// If this is not a new profile, then delete the previous choices
			if (!isNew) {
				prepareStatementWithoutLimits("DELETE FROM exams.QUESTIONMINFO WHERE (ID=?)");
				_ps.setInt(1, mqp.getID());
				executeUpdate(0);
			}

			// Write question choices
			prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)");
			_ps.setInt(1, mqp.getID());
			int seq = 0;
			for (Iterator<String> i = mqp.getChoices().iterator(); i.hasNext();) {
				_ps.setInt(2, ++seq);
				_ps.setString(3, i.next());
				_ps.addBatch();
			}

			// Execute the batch update
			_ps.executeBatch();
			_ps.close();

			// Commit
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
	public void write(CheckRideScript sc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO CR_DESCS (EQTYPE, EQPROGRAM, BODY) VALUES (?, ?, ?)");
			_ps.setString(1, sc.getEquipmentType());
			_ps.setString(2, sc.getProgram());
			_ps.setString(3, sc.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a Question Profile image resource to the database.
	 * @param qp the QuestionProfile bean
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if the Image bean is not populated
	 */
	public void writeImage(QuestionProfile qp) throws DAOException {

		// Check that we have data
		if (!qp.isLoaded())
			throw new IllegalArgumentException("Image Data not loaded");

		try {
			prepareStatement("REPLACE INTO exams.QUESTIONIMGS (ID, TYPE, X, Y, SIZE, IMG) VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, qp.getID());
			_ps.setInt(2, qp.getType());
			_ps.setInt(3, qp.getWidth());
			_ps.setInt(4, qp.getHeight());
			_ps.setInt(5, qp.getSize());
			_ps.setBinaryStream(6, qp.getInputStream(), qp.getSize());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates the Examinations that can use this Question.
	 * @param qp the QuestionProfile bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeExams(QuestionProfile qp) throws DAOException {
		try {
			startTransaction();
			
			// Remove the entries
			prepareStatementWithoutLimits("DELETE FROM exams.QE_INFO WHERE (QUESTION_ID=?)");
			_ps.setInt(1, qp.getID());
			executeUpdate(0);
			
			// Write the exam names
			prepareStatementWithoutLimits("INSERT INTO exams.QE_INFO (QUESTION_ID, EXAM_NAME) VALUES (?, ?)");
			_ps.setInt(1, qp.getID());
			for (Iterator<String> i = qp.getExamNames().iterator(); i.hasNext();) {
				String examName = i.next();
				_ps.setString(2, examName);
				_ps.addBatch();
			}

			// Execute the batch statement and clean up
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Examination Question profile from the database.
	 * @param qp the QuestionProfile bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(QuestionProfile qp) throws DAOException {
		try {
			prepareStatement("DELETE FROM exams.QUESTIONINFO WHERE (ID=?)");
			_ps.setInt(1, qp.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Check Ride Script from the database.
	 * @param sc the CheckRideScript bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(CheckRideScript sc) throws DAOException {
		try {
			prepareStatement("DELETE FROM CR_DESCS WHERE (EQTYPE=?)");
			_ps.setString(1, sc.getEquipmentType());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Question Profile image resource from the database.
	 * @param id the Question Profile database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearImage(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM exams.QUESTIONIMGS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object to write Examination Question profiles to the database. 
 * @author Luke
 * @version 8.6
 * @since 3.6
 */

public class SetExamQuestion extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetExamQuestion(Connection c) {
		super(c);
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
				prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONINFO (QUESTION, CORRECT, REFERENCE, ACTIVE, AIRLINE) VALUES (?, ?, ?, ?, ?)");
				_ps.setString(5, qp.getOwner().getCode());
			} else {
				prepareStatementWithoutLimits("DELETE FROM exams.QUESTIONAIRLINES WHERE (ID=?)");
				_ps.setInt(1, qp.getID());
				executeUpdate(0);
				prepareStatementWithoutLimits("UPDATE exams.QUESTIONINFO SET QUESTION=?, CORRECT=?, REFERENCE=?, ACTIVE=? WHERE (ID=?)");
				_ps.setInt(5, qp.getID());
			}

			// Set prepared statement and write the question
			_ps.setString(1, qp.getQuestion());
			_ps.setString(2, qp.getCorrectAnswer());
			_ps.setString(3, qp.getReference());
			_ps.setBoolean(4, qp.getActive());
			executeUpdate(1);

			// If this is a new question profile, get the ID back from the database and write stats, otherwise clear the exam names
			if (qp.getID() == 0) {
				qp.setID(getNewID());
				prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONSTATS (ID, TOTAL, CORRECT) VALUES (?, 0, 0)");
				_ps.setInt(1, qp.getID());
				executeUpdate(1);
			} else {
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
			for (String examName : qp.getExams()) {
				_ps.setString(2, examName);
				_ps.addBatch();
			}

			executeBatchUpdate(1, qp.getExams().size());
			
			// Write the airline names
			prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONAIRLINES (ID, AIRLINE) VALUES (?, ?)");
			_ps.setInt(1, qp.getID());
			for (AirlineInformation ai : qp.getAirlines()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}

			executeBatchUpdate(1, qp.getAirlines().size());

			// Write the multiple choice entries
			if (qp instanceof MultipleChoice) {
				MultipleChoice mq = (MultipleChoice) qp;
				prepareStatementWithoutLimits("INSERT INTO exams.QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)");
				_ps.setInt(1, qp.getID());

				// Write the entries
				int seq = 0;
				for (String choice : mq.getChoices()) {
					_ps.setInt(2, ++seq);
					_ps.setString(3, choice);
					_ps.addBatch();
				}

				executeBatchUpdate(1, mq.getChoices().size());
			}
			
			// Write the route plot entries
			if (qp instanceof RoutePlot) {
				RoutePlot rp = (RoutePlot) qp;
				prepareStatementWithoutLimits("REPLACE INTO exams.QUESTIONRPINFO (ID, AIRPORT_D, AIRPORT_A) VALUES (?, ?, ?)");
				_ps.setInt(1, qp.getID());
				_ps.setString(2, rp.getAirportD().getIATA());
				_ps.setString(3, rp.getAirportA().getIATA());
				executeUpdate(1);
			}

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
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
			for (String examName : qp.getExams()) {
				_ps.setString(2, examName);
				_ps.addBatch();
			}

			executeBatchUpdate(1, qp.getExams().size());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
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
}
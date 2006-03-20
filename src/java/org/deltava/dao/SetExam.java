// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Pilot Examinations and Check Rides to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetExam extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetExam(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Pilot Examination to the database. This will update the database ID for the Examination bean with
	 * the automatically generated ID column value.
	 * @param ex the Examination to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Examination ex) throws DAOException {

		try {
			startTransaction();

			// Prepare the statement for the examination
			prepareStatement("INSERT INTO EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, GRADED_ON, "
					+ "GRADED_BY, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, ex.getName());
			_ps.setInt(2, ex.getPilotID());
			_ps.setInt(3, ex.getStatus());
			_ps.setTimestamp(4, createTimestamp(ex.getDate()));
			_ps.setTimestamp(5, createTimestamp(ex.getSubmittedOn()));
			_ps.setTimestamp(6, createTimestamp(ex.getScoredOn()));
			_ps.setInt(7, ex.getScorerID());
			_ps.setTimestamp(8, createTimestamp(ex.getExpiryDate()));

			// Write the exam
			executeUpdate(1);

			// Get the new exam ID
			ex.setID(getNewID());

			// Prepare the statement for questions
			prepareStatement("INSERT INTO EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO, QUESTION, "
					+ "CORRECT_ANSWER) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, ex.getID());
			
			// Batch the questions
			for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext(); ) {
				Question q = i.next();
				_ps.setInt(2, q.getID());
				_ps.setInt(3, q.getNumber());
				_ps.setString(4, q.getQuestion());
				_ps.setString(5, q.getCorrectAnswer());
				_ps.addBatch();
			}

			// Write the questions
			_ps.executeBatch();
			
			// Write multiple-choice questions
			if (ex.hasMultipleChoice()) {
				prepareStatement("INSERT INTO EXAMQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)");
				_ps.setInt(1, ex.getID());
				for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext(); ) {
					Question q = i.next();
					if (q instanceof MultipleChoice) {
						MultipleChoice mq = (MultipleChoice) q;
						_ps.setInt(2, q.getID());
						
						// Save the choices
						int seq = 0;
						for (Iterator<String> ci = mq.getChoices().iterator(); ci.hasNext(); ) {
							String choice = ci.next();
							_ps.setInt(3, ++seq);
							_ps.setString(4, choice);
							_ps.addBatch();
						}
						
						_ps.executeBatch();
					}
				}
			}

			// Commit the transaction and clean up
			commitTransaction();
			_ps.close();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Pilot Examination in the database.
	 * @param ex the Examination to update
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Examination ex) throws DAOException {

		try {
			startTransaction();

			// Prepare the statement for the examination
			prepareStatement("UPDATE EXAMS SET STATUS=?, SUBMITTED_ON=?, GRADED_ON=?, GRADED_BY=?, PASS=?, "
					+ "COMMENTS=?, ISEMPTY=? WHERE (ID=?)");
			_ps.setInt(1, ex.getStatus());
			_ps.setTimestamp(2, createTimestamp(ex.getSubmittedOn()));
			_ps.setTimestamp(3, createTimestamp(ex.getScoredOn()));
			_ps.setInt(4, ex.getScorerID());
			_ps.setBoolean(5, ex.getPassFail());
			_ps.setString(6, ex.getComments());
			_ps.setBoolean(7, ex.getEmpty());
			_ps.setInt(8, ex.getID());

			// Update the exam
			executeUpdate(1);

			// Prepare the statement for questions
			prepareStatement("UPDATE EXAMQUESTIONS SET ANSWER=?, CORRECT=? WHERE (EXAM_ID=?) AND (QUESTION_ID=?)");
			_ps.setInt(3, ex.getID());

			// Batch the questions
			for (Iterator i = ex.getQuestions().iterator(); i.hasNext();) {
				Question q = (Question) i.next();
				_ps.setString(1, q.getAnswer());
				_ps.setBoolean(2, q.isCorrect());
				_ps.setInt(4, q.getID());
				_ps.addBatch();
			}

			// Update the questions
			_ps.executeBatch();
			_ps.close();

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a Check Ride to <i>the current database</i>. This can handle inserts and updates.
	 * @param cr the Check Ride object
	 * @throws DAOException if a JDBC error occurs
	 * @see SetExam#write(String, CheckRide)
	 */
	public void write(CheckRide cr) throws DAOException {
		write(SystemData.get("airline.db"), cr);
	}

	/**
	 * Writes a Check Ride to a database. This can handle inserts and updates.
	 * @param dbName the database name
	 * @param cr the Check Ride object
	 * @throws DAOException if a JDBC error occurs
	 * @see SetExam#write(CheckRide)
	 */
	public void write(String dbName, CheckRide cr) throws DAOException {
		try {
			// Prepare the statement, either an INSERT or an UPDATE
			if (cr.getID() == 0) {
				prepareStatement("INSERT INTO " + dbName.toLowerCase() + ".CHECKRIDES (NAME, PILOT_ID, ACARS_ID, "
						+ "STATUS, EQTYPE, ACTYPE, GRADED_BY, CREATED, SUBMITTED, COMMENTS, PASS) VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				_ps.setString(1, cr.getName());
				_ps.setInt(2, cr.getPilotID());
				_ps.setInt(3, cr.getFlightID());
				_ps.setInt(4, cr.getStatus());
				_ps.setString(5, cr.getEquipmentType());
				_ps.setString(6, cr.getAircraftType());
				_ps.setInt(7, cr.getScorerID());
				_ps.setTimestamp(8, createTimestamp(cr.getDate()));
				_ps.setTimestamp(9, createTimestamp(cr.getSubmittedOn()));
				_ps.setString(10, cr.getComments());
				_ps.setBoolean(11, cr.getPassFail());
			} else {
				prepareStatement("UPDATE " + dbName.toLowerCase() + ".CHECKRIDES SET STATUS=?, SUBMITTED=?, GRADED=?, "
						+ "ACARS_ID=?, GRADED_BY=?, PASS=?, COMMENTS=? WHERE (ID=?)");
				_ps.setInt(1, cr.getStatus());
				_ps.setTimestamp(2, createTimestamp(cr.getSubmittedOn()));
				_ps.setTimestamp(3, createTimestamp(cr.getScoredOn()));
				_ps.setInt(4, cr.getFlightID());
				_ps.setInt(5, cr.getScorerID());
				_ps.setBoolean(6, cr.getPassFail());
				_ps.setString(7, cr.getComments());
				_ps.setInt(8, cr.getID());
			}

			// Update the database
			executeUpdate(1);

			// Update the database ID
			if (cr.getID() == 0)
				cr.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Saves a single answer to an examination. <i>This is used by a web service</i>
	 * @param examID the Examination database ID
	 * @param q the Question bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void answer(int examID, Question q) throws DAOException {
		try {
			prepareStatement("UPDATE EXAMQUESTIONS SET ANSWER=? WHERE (EXAM_ID=?) AND (QUESTION_NO=?)");
			_ps.setString(1, q.getAnswer());
			_ps.setInt(2, examID);
			_ps.setInt(3, q.getNumber());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Pilot Examination or Check Ride from the database.
	 * @param t the Examination/Checkride
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Test t) throws DAOException {
		try {
			if (t instanceof Examination) {
				prepareStatement("DELETE FROM EXAMS WHERE (ID=?)");
			} else if (t instanceof CheckRide) {
				prepareStatement("DELETE FROM CHECKRIDES WHERE (ID=?)");
			}

			// Set the ID and update the database
			_ps.setInt(1, t.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
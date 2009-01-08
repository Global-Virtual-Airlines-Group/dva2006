// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

/**
 * A Data Access Object to write Pilot Examinations and Check Rides to the database.
 * @author Luke
 * @version 2.3
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
			prepareStatement("INSERT INTO exams.EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, "
					+ "GRADED_ON, GRADED_BY, AUTOSCORE, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, ex.getName());
			_ps.setInt(2, ex.getPilotID());
			_ps.setInt(3, ex.getStatus());
			_ps.setTimestamp(4, createTimestamp(ex.getDate()));
			_ps.setTimestamp(5, createTimestamp(ex.getSubmittedOn()));
			_ps.setTimestamp(6, createTimestamp(ex.getScoredOn()));
			_ps.setInt(7, ex.getScorerID());
			_ps.setBoolean(8, ex.getAutoScored());
			_ps.setTimestamp(9, createTimestamp(ex.getExpiryDate()));

			// Write the exam and get the new exam ID
			executeUpdate(1);
			ex.setID(getNewID());
			
			// Write the questions
			for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext(); ) {
				Question q = i.next();
				write(ex.getID(), q);
			}

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to write a Question to the database.
	 */
	private void write(int id, Question q) throws SQLException {
		prepareStatementWithoutLimits("INSERT INTO exams.EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO, "
				+ "QUESTION, CORRECT_ANSWER) VALUES (?, ?, ?, ?, ?)");
		_ps.setInt(1, id);
		_ps.setInt(2, q.getID());
		_ps.setInt(3, q.getNumber());
		_ps.setString(4, q.getQuestion());
		_ps.setString(5, q.getCorrectAnswer());
		executeUpdate(1);
		
		// Write child tables
		if (q instanceof MultiChoiceQuestion) {
			MultiChoiceQuestion mcq = (MultiChoiceQuestion) q;
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) "
					+ "VALUES (?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, q.getID());
			
			// Save the choices
			int seq = 0;
			for (Iterator<String> ci = mcq.getChoices().iterator(); ci.hasNext(); ) {
				String choice = ci.next();
				_ps.setInt(3, ++seq);
				_ps.setString(4, choice);
				_ps.addBatch();
			}
			
			_ps.executeBatch();
		} 
		
		if (q instanceof RoutePlotQuestion) {
			RoutePlotQuestion rpq = (RoutePlotQuestion) q;
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMQUESTIONSRP (EXAM_ID, QUESTION_ID, AIRPORT_D, " + 
					"AIRPORT_A) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, q.getID());
			_ps.setString(3, rpq.getAirportD().getIATA());
			_ps.setString(4, rpq.getAirportA().getIATA());
			executeUpdate(1);
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
			prepareStatement("UPDATE exams.EXAMS SET STATUS=?, SUBMITTED_ON=?, GRADED_ON=?, GRADED_BY=?, "
					+ "PASS=?, COMMENTS=?, ISEMPTY=?, AUTOSCORE=? WHERE (ID=?)");
			_ps.setInt(1, ex.getStatus());
			_ps.setTimestamp(2, createTimestamp(ex.getSubmittedOn()));
			_ps.setTimestamp(3, createTimestamp(ex.getScoredOn()));
			_ps.setInt(4, ex.getScorerID());
			_ps.setBoolean(5, ex.getPassFail());
			_ps.setString(6, ex.getComments());
			_ps.setBoolean(7, ex.getEmpty());
			_ps.setBoolean(8, ex.getAutoScored());
			_ps.setInt(9, ex.getID());

			// Update the exam
			executeUpdate(1);

			// Prepare the statement for questions
			prepareStatement("UPDATE exams.EXAMQUESTIONS SET ANSWER=?, CORRECT=? WHERE (EXAM_ID=?) AND (QUESTION_ID=?)");
			_ps.setInt(3, ex.getID());

			// Batch the questions
			for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext();) {
				Question q = i.next();
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
	 * Writes a Check Ride to the database. This can handle inserts and updates.
	 * @param cr the Check Ride object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(CheckRide cr) throws DAOException {
		try {
			startTransaction();
			
			// Prepare the statement, either an INSERT or an UPDATE
			if (cr.getID() == 0) {
				prepareStatement("INSERT INTO exams.CHECKRIDES (NAME, PILOT_ID, STATUS, EQTYPE, ACTYPE, "
						+ "GRADED_BY, CREATED, SUBMITTED, COMMENTS, PASS, ACADEMY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				_ps.setString(1, cr.getName());
				_ps.setInt(2, cr.getPilotID());
				_ps.setInt(3, cr.getStatus());
				_ps.setString(4, cr.getEquipmentType());
				_ps.setString(5, cr.getAircraftType());
				_ps.setInt(6, cr.getScorerID());
				_ps.setTimestamp(7, createTimestamp(cr.getDate()));
				_ps.setTimestamp(8, createTimestamp(cr.getSubmittedOn()));
				_ps.setString(9, cr.getComments());
				_ps.setBoolean(10, cr.getPassFail());
				_ps.setBoolean(11, cr.getAcademy());
			} else {
				prepareStatement("UPDATE exams.CHECKRIDES SET STATUS=?, SUBMITTED=?, GRADED=?, GRADED_BY=?, "
						+ "PASS=?, COMMENTS=? WHERE (ID=?)");
				_ps.setInt(1, cr.getStatus());
				_ps.setTimestamp(2, createTimestamp(cr.getSubmittedOn()));
				_ps.setTimestamp(3, createTimestamp(cr.getScoredOn()));
				_ps.setInt(4, cr.getScorerID());
				_ps.setBoolean(5, cr.getPassFail());
				_ps.setString(6, cr.getComments());
				_ps.setInt(7, cr.getID());
			}

			// Update the database
			executeUpdate(1);

			// Update the database ID
			if (cr.getID() == 0)
				cr.setID(getNewID());
			
			// Write the ACARS Flight ID
			if (cr.getFlightID() != 0) {
				prepareStatement("REPLACE INTO exams.CHECKRIDE_FLIGHTS (ID, ACARS_ID) VALUES (?, ?)");
				_ps.setInt(2, cr.getFlightID());
			} else
				prepareStatement("DELETE FROM exams.CHECKRIDE_FLIGHTS WHERE (ID=?)");
			
			// Do the write
			_ps.setInt(1, cr.getID());
			executeUpdate(0);
			
			// Write the Flight Academy data
			if (cr.getCourseID() != 0)
				linkCheckRide(cr);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Links a CheckRide to a Flight Academy course.
	 * @param cr the CheckRide bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void linkCheckRide(CheckRide cr) throws DAOException {
		try {
			if (cr.getCourseID() == 0)
				prepareStatement("DELETE FROM exams.COURSERIDES WHERE (CHECKRIDE=?)");
			else {
				prepareStatement("REPLACE INTO exams.COURSERIDES (CHECKRIDE, COURSE) VALUES (?, ?)");
				_ps.setInt(2, cr.getCourseID());
			}
			
			// Execute the statement
			_ps.setInt(1, cr.getID());
			executeUpdate(1);
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
			prepareStatement("UPDATE exams.EXAMQUESTIONS SET ANSWER=? WHERE (EXAM_ID=?) AND (QUESTION_NO=?)");
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
			if (t instanceof Examination)
				prepareStatement("DELETE FROM exams.EXAMS WHERE (ID=?)");
			else if (t instanceof CheckRide)
				prepareStatement("DELETE FROM exams.CHECKRIDES WHERE (ID=?)");

			// Set the ID and update the database
			_ps.setInt(1, t.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
// Copyright 2005, 2007, 2011, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicant Questionnaires.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class SetQuestionnaire extends DAO {
	
	private final String _qName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME;

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetQuestionnaire(Connection c) {
		super(c);
	}

	/**
	 * Writes an Applicant Questionnaire to the database. This can handle INSERTs and UPDATEs.
	 * @param e the Examination bean
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if the Examination name is not &quot;Initial Questionnaire&quot;
	 */
	public void write(Examination e) throws DAOException {

		// Check the exam name
		if (!_qName.equals(e.getName()))
			throw new IllegalArgumentException("Invalid Examination - " + e.getName());

		// Check if we're adding or updating
		boolean isNew = (e.getID() == 0);
		try {
			startTransaction();

			// Create the prepared statement
			if (isNew)
				prepareStatement("INSERT INTO APPEXAMS (APP_ID, STATUS, CREATED_ON, EXPIRY_TIME, SUBMITTED_ON, GRADED_ON, GRADED_BY, AUTOSCORE, COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			else {
				prepareStatement("UPDATE APPEXAMS SET APP_ID=?, STATUS=?, CREATED_ON=?, EXPIRY_TIME=?, SUBMITTED_ON=?, GRADED_ON=?, GRADED_BY=?, AUTOSCORE=?, COMMENTS=? WHERE (ID=?)");
				_ps.setInt(10, e.getID());
			}

			// Add the prepared statement arguments
			_ps.setInt(1, e.getAuthorID());
			_ps.setInt(2, e.getStatus().ordinal());
			_ps.setTimestamp(3, createTimestamp(e.getDate()));
			_ps.setTimestamp(4, createTimestamp(e.getExpiryDate()));
			_ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
			_ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
			_ps.setInt(7, e.getScorerID());
			_ps.setBoolean(8, e.getAutoScored());
			_ps.setString(9, e.getComments());
			executeUpdate(1);

			// If we're writing a new exam, get the ID
			if (isNew)
				e.setID(getNewID());

			// Prepare the statement for the questions
			if (isNew)
				prepareStatement("INSERT INTO APPQUESTIONS (QUESTION_ID, QUESTION, CORRECT_ANSWER, ANSWER, CORRECT, EXAM_ID, QUESTION_NO) VALUES (?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE APPQUESTIONS SET QUESTION_ID=?, QUESTION=?, CORRECT_ANSWER=?, ANSWER=?, CORRECT=? WHERE (EXAM_ID=?) AND (QUESTION_NO=?)");

			// Add the questions
			_ps.setInt(6, e.getID());
			for (Question q : e.getQuestions()) {
				_ps.setInt(1, q.getID());
				_ps.setString(2, q.getQuestion());
				_ps.setString(3, q.getCorrectAnswer());
				_ps.setString(4, q.getAnswer());
				_ps.setBoolean(5, q.isCorrect());
				_ps.setInt(7, q.getNumber());
				_ps.addBatch();
			}

			executeBatchUpdate(1, e.getQuestions().size());

			// Save multiple choice answers
			if (e.hasMultipleChoice() && isNew) {
				prepareStatement("INSERT INTO APPQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)");
				_ps.setInt(1, e.getID());
				for (Iterator<Question> i = e.getQuestions().iterator(); i.hasNext();) {
					Question q = i.next();
					if (q instanceof MultiChoiceQuestion) {
						_ps.setInt(2, q.getID());
						MultiChoiceQuestion mq = (MultiChoiceQuestion) q;

						// Save the choices
						int seq = 0;
						for (String choice : mq.getChoices()) {
							_ps.setInt(3, ++seq);
							_ps.setString(4, choice);
							_ps.addBatch();
						}

						_ps.executeBatch();
					}
				}

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
	 * Deletes an Applicant Questionnaire from the database.
	 * @param id the Questionnaire database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM APPEXAMS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Converts an Applicant Questionnaire into a Pilot Examination (when an Applicant is hired).
	 * @param e the Questionnaire bean
	 * @param pilotID the new Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if the Examination name is not &quot;Initial Questionnaire&quot;
	 */
	public void convertToExam(Examination e, int pilotID) throws DAOException {

		// Check the exam name
		if (!_qName.equals(e.getName()))
			throw new IllegalArgumentException("Invalid Examination - " + e.getName());

		try {
			startTransaction();

			// Create the Examination prepared statement
			prepareStatement("INSERT INTO exams.EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, GRADED_ON, GRADED_BY, EXPIRY_TIME, PASS, AUTOSCORE, COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, e.getName());
			_ps.setInt(2, pilotID);
			_ps.setInt(3, e.getStatus().ordinal());
			_ps.setTimestamp(4, createTimestamp(e.getDate()));
			_ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
			_ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
			_ps.setInt(7, e.getScorerID());
			_ps.setTimestamp(8, createTimestamp(e.getExpiryDate()));
			_ps.setBoolean(9, true);
			_ps.setBoolean(10, e.getAutoScored());
			_ps.setString(11, e.getComments());

			// Write the exam and get the new exam ID
			executeUpdate(1);
			int examID = getNewID();

			// Write the questions
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO, CORRECT) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, examID);
			for (Question q : e.getQuestions()) {
				_ps.setInt(2, q.getID());
				_ps.setInt(3, q.getNumber());
				_ps.setBoolean(4, q.isCorrect());
				_ps.addBatch();
			}

			executeBatchUpdate(1, e.getQuestions().size());
			
			// Write the answers
			prepareStatementWithoutLimits("INSERT INTO exams.EXAMQANSWERS (EXAM_ID, QUESTION_NO, QUESTION, CORRECT_ANSWER, ANSWER) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, examID);
			for (Question q : e.getQuestions()) {
				_ps.setInt(2, q.getNumber());
				_ps.setString(3, q.getQuestion());
				_ps.setString(4, q.getCorrectAnswer());
				_ps.setString(5, q.getAnswer());
				_ps.addBatch();
			}

			executeBatchUpdate(1, e.getQuestions().size());
			
			// Copy multiple choice data
			if (e.hasMultipleChoice()) {
				prepareStatementWithoutLimits("INSERT INTO exams.EXAMQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)");
				_ps.setInt(1, examID);

				// Batch the questions
				for (Question q : e.getQuestions()) {
					if (q instanceof MultiChoiceQuestion) {
						MultiChoiceQuestion mq = (MultiChoiceQuestion) q;
						_ps.setInt(2, mq.getID());

						// Save the questions
						int seq = 0;
						for (String choice : mq.getChoices()) {
							_ps.setInt(3, ++seq);
							_ps.setString(4, choice);
							_ps.addBatch();
						}

						_ps.executeBatch();
					}
				}

				_ps.close();
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
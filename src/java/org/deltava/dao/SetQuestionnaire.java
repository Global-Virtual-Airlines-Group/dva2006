// Copyright 2005, 2007, 2011, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicant Questionnaires.
 * @author Luke
 * @version 9.0
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
	 * Writes an Applicant Questionnaire to the database.
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

			// Create the questionnaire
			try (PreparedStatement ps = prepare("INSERT INTO APPEXAMS (APP_ID, STATUS, CREATED_ON, EXPIRY_TIME, SUBMITTED_ON, GRADED_ON, GRADED_BY, AUTOSCORE, COMMENTS, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE APP_ID=VALUES(APP_ID), STATUS=VALUES(STATUS), CREATED_ON=VALUES(CREATED_ON), EXPIRY_TIME=VALUES(EXPIRY_TIME), SUBMITTED_ON=VALUES(SUBMITTED_ON), "
				+ "GRADED_ON=VALUES(GRADED_ON), AUTOSCORE=VALUES(AUTOSCORE), COMMENTS=VALUES(COMMENTS)")) {
				ps.setInt(1, e.getAuthorID());
				ps.setInt(2, e.getStatus().ordinal());
				ps.setTimestamp(3, createTimestamp(e.getDate()));
				ps.setTimestamp(4, createTimestamp(e.getExpiryDate()));
				ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
				ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
				ps.setInt(7, e.getScorerID());
				ps.setBoolean(8, e.getAutoScored());
				ps.setString(9, e.getComments());
				ps.setInt(10, e.getID());
				executeUpdate(ps, 1);
			}

			// If we're writing a new exam, get the ID
			if (isNew) e.setID(getNewID());

			// Write the questions
			try (PreparedStatement ps = prepare("INSERT INTO APPQUESTIONS (QUESTION_ID, QUESTION, CORRECT_ANSWER, ANSWER, CORRECT, EXAM_ID, QUESTION_NO) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
				+ "QUESTION_ID=VALUES(QUESTION_ID), QUESTION=VALUES(QUESTION), CORRECT_ANSWER=VALUES(CORRECT_ANSWER), ANSWER=VALUES(ANSWER), CORRECT=VALUES(CORRECT)")) {
				ps.setInt(6, e.getID());
				for (Question q : e.getQuestions()) {
					ps.setInt(1, q.getID());
					ps.setString(2, q.getQuestion());
					ps.setString(3, q.getCorrectAnswer());
					ps.setString(4, q.getAnswer());
					ps.setBoolean(5, q.isCorrect());
					ps.setInt(7, q.getNumber());
					ps.addBatch();
				}

				executeUpdate(ps, 1, e.getQuestions().size());
			}

			// Save multiple choice answers
			if (e.hasMultipleChoice() && isNew) {
				try (PreparedStatement ps = prepare("INSERT INTO APPQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)")) {
					ps.setInt(1, e.getID());
					for (Question q : e.getQuestions()) {
						if (q instanceof MultiChoiceQuestion) {
							ps.setInt(2, q.getID());
							MultiChoiceQuestion mq = (MultiChoiceQuestion) q;
							
							// Save the choices
							int seq = 0;
							for (String choice : mq.getChoices()) {
								ps.setInt(3, ++seq);
								ps.setString(4, choice);
								ps.addBatch();
							}	

							ps.executeBatch();
						}
					}
				}
			}

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
		try (PreparedStatement ps = prepare("DELETE FROM APPEXAMS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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

			// Create the Examination
			try (PreparedStatement ps = prepare("INSERT INTO exams.EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, GRADED_ON, GRADED_BY, EXPIRY_TIME, PASS, AUTOSCORE, COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, e.getName());
				ps.setInt(2, pilotID);
				ps.setInt(3, e.getStatus().ordinal());
				ps.setTimestamp(4, createTimestamp(e.getDate()));
				ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
				ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
				ps.setInt(7, e.getScorerID());
				ps.setTimestamp(8, createTimestamp(e.getExpiryDate()));
				ps.setBoolean(9, true);
				ps.setBoolean(10, e.getAutoScored());
				ps.setString(11, e.getComments());
				executeUpdate(ps, 1);
			}
			
			int examID = getNewID();

			// Write the questions
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO, CORRECT) VALUES (?, ?, ?, ?)")) {
				ps.setInt(1, examID);
				for (Question q : e.getQuestions()) {
					ps.setInt(2, q.getID());
					ps.setInt(3, q.getNumber());
					ps.setBoolean(4, q.isCorrect());
					ps.addBatch();
				}

				executeUpdate(ps, 1, e.getQuestions().size());
			}
			
			// Write the answers
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQANSWERS (EXAM_ID, QUESTION_NO, QUESTION, CORRECT_ANSWER, ANSWER) VALUES (?, ?, ?, ?, ?)")) {
				ps.setInt(1, examID);
				for (Question q : e.getQuestions()) {
					ps.setInt(2, q.getNumber());
					ps.setString(3, q.getQuestion());
					ps.setString(4, q.getCorrectAnswer());
					ps.setString(5, q.getAnswer());
					ps.addBatch();
				}

				executeUpdate(ps, 1, e.getQuestions().size());
			}
			
			// Copy multiple choice data
			if (e.hasMultipleChoice()) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)")) {
					ps.setInt(1, examID);

					// Batch the questions
					for (Question q : e.getQuestions()) {
						if (q instanceof MultiChoiceQuestion) {
							MultiChoiceQuestion mq = (MultiChoiceQuestion) q;
							ps.setInt(2, mq.getID());

							// Save the questions
							int seq = 0;
							for (String choice : mq.getChoices()) {
								ps.setInt(3, ++seq);
								ps.setString(4, choice);
								ps.addBatch();
							}

							ps.executeBatch();
						}
					}
				}
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
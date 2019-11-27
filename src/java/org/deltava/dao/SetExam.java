// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;

/**
 * A Data Access Object to write Pilot Examinations and Check Rides to the database.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO exams.EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, GRADED_ON, GRADED_BY, AUTOSCORE, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, ex.getName());
				ps.setInt(2, ex.getAuthorID());
				ps.setInt(3, ex.getStatus().ordinal());
				ps.setTimestamp(4, createTimestamp(ex.getDate()));
				ps.setTimestamp(5, createTimestamp(ex.getSubmittedOn()));
				ps.setTimestamp(6, createTimestamp(ex.getScoredOn()));
				ps.setInt(7, ex.getScorerID());
				ps.setBoolean(8, ex.getAutoScored());
				ps.setTimestamp(9, createTimestamp(ex.getExpiryDate()));

				// Write the exam and get the new exam ID
				executeUpdate(ps, 1);
				ex.setID(getNewID());
			}
			
			// Write the questions
			for (Question q : ex.getQuestions())
				write(ex.getID(), q);

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to write a Question to the database.
	 */
	private void write(int id, Question q) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO) VALUES (?, ?, ?)")) {
			ps.setInt(1, id);
			ps.setInt(2, q.getID());
			ps.setInt(3, q.getNumber());
			executeUpdate(ps, 1);
		}
		
		// Write question text
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQANSWERS (EXAM_ID, QUESTION_NO, QUESTION, REFERENCE, CORRECT_ANSWER) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, id);
			ps.setInt(2, q.getNumber());
			ps.setString(3, q.getQuestion());
			ps.setString(4, q.getReference());
			ps.setString(5, q.getCorrectAnswer());
			executeUpdate(ps, 1);
		}
		
		// Write child tables
		if (q instanceof MultiChoiceQuestion) {
			MultiChoiceQuestion mcq = (MultiChoiceQuestion) q;
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQUESTIONSM (EXAM_ID, QUESTION_ID, SEQ, ANSWER) VALUES (?, ?, ?, ?)")) {
				ps.setInt(1, id);
				ps.setInt(2, q.getID());
			
				// Save the choices
				int seq = 0;
				for (String choice : mcq.getChoices()) {
					ps.setInt(3, ++seq);
					ps.setString(4, choice);
					ps.addBatch();
				}

				executeUpdate(ps, 1, mcq.getChoices().size());
			}
		} 
		
		if (q instanceof RoutePlotQuestion) {
			RoutePlotQuestion rpq = (RoutePlotQuestion) q;
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.EXAMQUESTIONSRP (EXAM_ID, QUESTION_ID, AIRPORT_D, AIRPORT_A) VALUES (?, ?, ?, ?)")) {
				ps.setInt(1, id);
				ps.setInt(2, q.getID());
				ps.setString(3, rpq.getAirportD().getIATA());
				ps.setString(4, rpq.getAirportA().getIATA());
				executeUpdate(ps, 1);
			}
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
			try (PreparedStatement ps = prepare("UPDATE exams.EXAMS SET STATUS=?, SUBMITTED_ON=?, GRADED_ON=?, GRADED_BY=?, PASS=?, COMMENTS=?, ISEMPTY=?, AUTOSCORE=? WHERE (ID=?)")) {
				ps.setInt(1, ex.getStatus().ordinal());
				ps.setTimestamp(2, createTimestamp(ex.getSubmittedOn()));
				ps.setTimestamp(3, createTimestamp(ex.getScoredOn()));
				ps.setInt(4, ex.getScorerID());
				ps.setBoolean(5, ex.getPassFail());
				ps.setString(6, ex.getComments());
				ps.setBoolean(7, ex.getEmpty());
				ps.setBoolean(8, ex.getAutoScored());
				ps.setInt(9, ex.getID());
				executeUpdate(ps, 1);
			}

			// Prepare the statement for questions
			try (PreparedStatement ps = prepare("UPDATE exams.EXAMQUESTIONS EQ, exams.EXAMQANSWERS EQA SET EQA.ANSWER=?, EQ.CORRECT=? WHERE (EQ.EXAM_ID=EQA.EXAM_ID) AND "
				+ "(EQ.QUESTION_NO=EQA.QUESTION_NO) AND (EQ.EXAM_ID=?) AND (EQ.QUESTION_NO=?)")) {
				ps.setInt(3, ex.getID());
				for (Question q : ex.getQuestions()) {
					ps.setString(1, q.getAnswer());
					ps.setBoolean(2, q.isCorrect());
					ps.setInt(4, q.getNumber());
					ps.addBatch();
				}

				executeUpdate(ps, 1, ex.getQuestions().size());
			}
			
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
				try (PreparedStatement ps = prepare("INSERT INTO exams.CHECKRIDES (NAME, PILOT_ID, STATUS, EQTYPE, ACTYPE, GRADED_BY, CREATED, SUBMITTED, COMMENTS, PASS, TYPE, EXPIRES, GRADED, ACADEMY, OWNER) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
					ps.setString(1, cr.getName());
					ps.setInt(2, cr.getAuthorID());
					ps.setInt(3, cr.getStatus().ordinal());
					ps.setString(4, cr.getEquipmentType());
					ps.setString(5, cr.getAircraftType());
					ps.setInt(6, cr.getScorerID());
					ps.setTimestamp(7, createTimestamp(cr.getDate()));
					ps.setTimestamp(8, createTimestamp(cr.getSubmittedOn()));
					ps.setString(9, cr.getComments());
					ps.setBoolean(10, cr.getPassFail());
					ps.setInt(11, cr.getType().ordinal());
					ps.setTimestamp(12, createTimestamp(cr.getExpirationDate()));
					ps.setTimestamp(13, createTimestamp(cr.getScoredOn()));
					ps.setBoolean(14, cr.getAcademy());
					ps.setString(15, cr.getOwner().getCode());
					executeUpdate(ps, 1);
				}
			} else {
				try (PreparedStatement ps = prepare("UPDATE exams.CHECKRIDES SET STATUS=?, SUBMITTED=?, GRADED=?, GRADED_BY=?, PASS=?, COMMENTS=?, EXPIRES=? WHERE (ID=?)")) {
					ps.setInt(1, cr.getStatus().ordinal());
					ps.setTimestamp(2, createTimestamp(cr.getSubmittedOn()));
					ps.setTimestamp(3, createTimestamp(cr.getScoredOn()));
					ps.setInt(4, cr.getScorerID());
					ps.setBoolean(5, cr.getPassFail());
					ps.setString(6, cr.getComments());
					ps.setTimestamp(7, createTimestamp(cr.getExpirationDate()));
					ps.setInt(8, cr.getID());
					executeUpdate(ps, 1);
				}
			}

			if (cr.getID() == 0)
				cr.setID(getNewID());
			
			// Write the ACARS Flight ID
			if (cr.getFlightID() != 0) {
				try (PreparedStatement ps =prepare("REPLACE INTO exams.CHECKRIDE_FLIGHTS (ID, ACARS_ID) VALUES (?, ?)")) {
					ps.setInt(1, cr.getID());	
					ps.setInt(2, cr.getFlightID());
					executeUpdate(ps, 0);
				}
			} else {
				try (PreparedStatement ps = prepare("DELETE FROM exams.CHECKRIDE_FLIGHTS WHERE (ID=?)")) {
					ps.setInt(1, cr.getID());
					executeUpdate(ps, 0);
				}
			}
			
			// Write the Flight Academy data
			linkCheckRide(cr);
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
				try (PreparedStatement ps = prepare("DELETE FROM exams.COURSERIDES WHERE (CHECKRIDE=?)")) {
					ps.setInt(1, cr.getID());
					executeUpdate(ps, 0);
				}
			else {
				try (PreparedStatement ps = prepare("REPLACE INTO exams.COURSERIDES (CHECKRIDE, COURSE) VALUES (?, ?)")) {
					ps.setInt(1, cr.getID());
					ps.setInt(2, cr.getCourseID());
					executeUpdate(ps, 0);
				}
			}
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
		try (PreparedStatement ps = prepare("UPDATE exams.EXAMQANSWERS SET ANSWER=? WHERE (EXAM_ID=?) AND (QUESTION_NO=?)")) {
			ps.setString(1, q.getAnswer());
			ps.setInt(2, examID);
			ps.setInt(3, q.getNumber());
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates Examination correct answer statistics.
	 * @param e the Examination bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateStats(Examination e) throws DAOException {
		try (PreparedStatement ps = prepare("REPLACE INTO exams.QUESTIONSTATS (SELECT EQ.QUESTION_ID, ?, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM exams.EXAMQUESTIONS EQ, exams.EXAMS E, "
			+ "exams.EXAMINFO EP WHERE (EQ.EXAM_ID=E.ID) AND (EP.NAME=E.NAME) AND (E.ISEMPTY=?) AND (EP.ACADEMY=?) AND (EQ.QUESTION_ID=?) GROUP BY EQ.QUESTION_ID)")) {
			ps.setBoolean(1, e.getAcademy());
			ps.setBoolean(2, false);
			ps.setBoolean(3, e.getAcademy());
			for (Question q : e.getQuestions()) {
				ps.setInt(4, q.getID());	
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, e.getQuestions().size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes unflown Check Rides from a Flight Academy Course.
	 * @param courseID the database ID of the Course
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteCheckRides(int courseID) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE CR FROM exams.CHECKRIDES CR, exams.COURSERIDES CCR WHERE (CR.ID=CCR.CHECKRIDE) AND (CCR.COURSE=?) AND (CR.STATUS=?)")) {
			ps.setInt(1, courseID);
			ps.setInt(2, TestStatus.NEW.ordinal());
			executeUpdate(ps, 0);
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
				try (PreparedStatement ps = prepare("DELETE FROM exams.EXAMS WHERE (ID=?)")) {
					ps.setInt(1, t.getID());
					executeUpdate(ps, 1);
				}
			} else if (t instanceof CheckRide) {
				try (PreparedStatement ps = prepare("DELETE FROM exams.CHECKRIDES WHERE (ID=?)")) {
					ps.setInt(1, t.getID());
					executeUpdate(ps, 1);		
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
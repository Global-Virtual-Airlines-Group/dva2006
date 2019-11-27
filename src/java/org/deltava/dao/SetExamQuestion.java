// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object to write Examination Question profiles to the database. 
 * @author Luke
 * @version 9.0
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
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QUESTIONINFO (QUESTION, CORRECT, REFERENCE, ACTIVE, AIRLINE) VALUES (?, ?, ?, ?, ?)")) {
					ps.setString(1, qp.getQuestion());
					ps.setString(2, qp.getCorrectAnswer());
					ps.setString(3, qp.getReference());
					ps.setBoolean(4, qp.getActive());
					ps.setString(5, qp.getOwner().getCode());
					executeUpdate(ps, 1);
				}
				
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.QUESTIONAIRLINES WHERE (ID=?)")) {
					ps.setInt(1, qp.getID());
					executeUpdate(ps, 0);
				}
				
				try (PreparedStatement ps = prepare("UPDATE exams.QUESTIONINFO SET QUESTION=?, CORRECT=?, REFERENCE=?, ACTIVE=? WHERE (ID=?)")) {
					ps.setString(1, qp.getQuestion());
					ps.setString(2, qp.getCorrectAnswer());
					ps.setString(3, qp.getReference());
					ps.setBoolean(4, qp.getActive());
					ps.setInt(5, qp.getID());
					executeUpdate(ps, 1);		
				}
			}

			// If this is a new question profile, get the ID back from the database and write stats, otherwise clear the exam names
			if (qp.getID() == 0) {
				qp.setID(getNewID());
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QUESTIONSTATS (ID, TOTAL, CORRECT) VALUES (?, 0, 0)")) {
					ps.setInt(1, qp.getID());
					executeUpdate(ps, 1);
				}
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.QE_INFO WHERE (QUESTION_ID=?)")) {
					ps.setInt(1, qp.getID());
					executeUpdate(ps, 0);
				}

				// Clear out the multiple choice options if we have them
				if (qp instanceof MultipleChoice) {
					try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.QUESTIONMINFO WHERE (ID=?)")) {
						ps.setInt(1, qp.getID());
						executeUpdate(ps, 0);
					}
				}
			}

			// Write the exam names
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QE_INFO (QUESTION_ID, EXAM_NAME) VALUES (?, ?)")) {
				ps.setInt(1, qp.getID());
				for (String examName : qp.getExams()) {
					ps.setString(2, examName);
					ps.addBatch();
				}

				executeUpdate(ps, 1, qp.getExams().size());
			}
			
			// Write the airline names
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QUESTIONAIRLINES (ID, AIRLINE) VALUES (?, ?)")) {
				ps.setInt(1, qp.getID());
				for (AirlineInformation ai : qp.getAirlines()) {
					ps.setString(2, ai.getCode());
					ps.addBatch();
				}

				executeUpdate(ps, 1, qp.getAirlines().size());
			}

			// Write the multiple choice entries
			if (qp instanceof MultipleChoice) {
				MultipleChoice mq = (MultipleChoice) qp;
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)")) {
					ps.setInt(1, qp.getID());

					// Write the entries
					int seq = 0;
					for (String choice : mq.getChoices()) {
						ps.setInt(2, ++seq);
						ps.setString(3, choice);
						ps.addBatch();
					}

					executeUpdate(ps, 1, mq.getChoices().size());
				}
			}
			
			// Write the route plot entries
			if (qp instanceof RoutePlot) {
				RoutePlot rp = (RoutePlot) qp;
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.QUESTIONRPINFO (ID, AIRPORT_D, AIRPORT_A) VALUES (?, ?, ?)")) {
					ps.setInt(1, qp.getID());
					ps.setString(2, rp.getAirportD().getIATA());
					ps.setString(3, rp.getAirportA().getIATA());
					executeUpdate(ps, 1);
				}
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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.QE_INFO WHERE (QUESTION_ID=?)")) {
				ps.setInt(1, qp.getID());
				executeUpdate(ps, 0);
			}
			
			// Write the exam names
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.QE_INFO (QUESTION_ID, EXAM_NAME) VALUES (?, ?)")) {
				ps.setInt(1, qp.getID());
				for (String examName : qp.getExams()) {
					ps.setString(2, examName);
					ps.addBatch();
				}

				executeUpdate(ps, 1, qp.getExams().size());
			}
			
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

		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.QUESTIONIMGS (ID, TYPE, X, Y, SIZE, IMG) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, qp.getID());
			ps.setInt(2, qp.getType());
			ps.setInt(3, qp.getWidth());
			ps.setInt(4, qp.getHeight());
			ps.setInt(5, qp.getSize());
			ps.setBinaryStream(6, qp.getInputStream(), qp.getSize());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.QUESTIONIMGS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.QUESTIONINFO WHERE (ID=?)")) {
			ps.setInt(1, qp.getID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
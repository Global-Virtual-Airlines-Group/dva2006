// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Examination questions.
 * @author Luke
 * @version 7.2
 * @since 2.1
 */

public class GetExamQuestions extends DAO {
	
	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamQuestions(Connection c) {
		super(c);
	}
	
	/**
	 * Loads a Question Profile.
	 * @param id the Question ID
	 * @return the Question profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public QuestionProfile getQuestionProfile(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, "
				+ "QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS "
				+ "ON (Q.ID=QS.ID) LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ "
				+ "ON (MQ.ID=Q.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (RQ.ID=Q.ID) WHERE (Q.ID=?) GROUP BY Q.ID LIMIT 1");
			_ps.setInt(1, id);

			// Execute the Query
			QuestionProfile qp = null; boolean isMultiChoice = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (!rs.next()) {
					_ps.close();
					return null;
				}
			
				// Check if we are multiple choice/have an image
				isMultiChoice = (rs.getInt(8) > 0);
				boolean isImage = (rs.getInt(10) > 0);
				boolean isRP = (rs.getString(14) != null);

				// Populate the Question Profile
				if (isRP) {
					RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
					rpqp.setAirportD(SystemData.getAirport(rs.getString(13)));
					rpqp.setAirportA(SystemData.getAirport(rs.getString(14)));
					qp = rpqp;
				} else if (isMultiChoice)
					qp = new MultiChoiceQuestionProfile(rs.getString(2));
				else
					qp = new QuestionProfile(rs.getString(2));
			
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setActive(rs.getBoolean(4));
				qp.setOwner(SystemData.getApp(rs.getString(5)));
				qp.setTotalAnswers(rs.getInt(6));
				qp.setCorrectAnswers(rs.getInt(7));

				// Load image data
				if (isImage) {
					qp.setType(rs.getInt(9));
					qp.setSize(rs.getInt(10));
					qp.setWidth(rs.getInt(11));
					qp.setHeight(rs.getInt(12));
				}
			}

			_ps.close();
			
			// Load airlines
			prepareStatementWithoutLimits("SELECT AIRLINE FROM exams.QUESTIONAIRLINES WHERE (ID=?)");
			_ps.setInt(1, qp.getID());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					qp.addAirline(SystemData.getApp(rs.getString(1)));
			}
			
			_ps.close();

			// Get multiple choice choices
			if (isMultiChoice) {
				MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) qp;
				prepareStatementWithoutLimits("SELECT ANSWER FROM exams.QUESTIONMINFO WHERE (ID=?) ORDER BY SEQ");
				_ps.setInt(1, qp.getID());
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next())
					mqp.addChoice(rs.getString(1));	
				}

				_ps.close();
			}
			
			// Get the exams for this question
			prepareStatementWithoutLimits("SELECT QE.EXAM_NAME FROM exams.QE_INFO QE WHERE (QE.QUESTION_ID=?)");
			_ps.setInt(1, id);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
				qp.addExam(rs.getString(1));	
			}

			_ps.close();
			return qp;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads Questions for a particular Examination, or all Examinations. 
	 * @param exam the ExaminationProfile or null if all
	 * @return a List of QuestionProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getQuestions(ExamProfile exam) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, "
				+ "QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS "
				+ "ON (Q.ID=QS.ID) LEFT JOIN exams.QE_INFO QE ON (Q.ID=QE.QUESTION_ID) LEFT JOIN exams.QUESTIONIMGS QI "
				+ "ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ "
				+ "ON (Q.ID=RQ.ID) ");
		if (exam != null)
			sqlBuf.append("WHERE (QE.EXAM_NAME=?) ");
		sqlBuf.append("GROUP BY Q.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			if (exam != null)
				_ps.setString(1, exam.getName());
			
			// Load results
			List<QuestionProfile> results = execute();
			if (exam != null)
				loadMultiChoice(exam.getName(), results);

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the most commonly asked active examination Questions.
	 * @param isAcademy TRUE if Academy examinations only, otherwise FALSE
	 * @return a List of QuestionProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getMostPopular(boolean isAcademy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, "
			+ "RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS ON ((Q.ID=QS.ID)");
		if (isAcademy)
			sqlBuf.append(" AND (QS.ACADEMY=?)");
		sqlBuf.append(")	LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) "
			+ "LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) WHERE (Q.ACTIVE=?) GROUP BY Q.ID ORDER BY QS.TOTAL DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			if (isAcademy)
				_ps.setBoolean(2, true);
			
			List<QuestionProfile> results = execute();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns active examination Questions based on the frequency of being answered correctly.
	 * @param isDesc TRUE if in descening order of correct answers, otherwise FALSE
	 * @param isAcademy TRUE if Academy examinations only, otherwise FALSE
	 * @param minExams the minimum number of exams the Question must have been in
	 * @return a List of QuestionProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getResults(boolean isDesc, boolean isAcademy, int minExams) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, "
			+ "QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS ON "
			+ "((Q.ID=QS.ID)");
		if (isAcademy)
			sqlBuf.append(" AND (QS.ACADEMY=?)");
		sqlBuf.append(") LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON "
			+ "(Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) WHERE (Q.ACTIVE=?) GROUP BY Q.ID "
			+ "HAVING (QS.TOTAL>=?) ORDER BY (QS.CORRECT/QS.TOTAL)");
		if (isDesc)
			sqlBuf.append(" DESC");
		
		try {
			int param = 0;
			prepareStatement(sqlBuf.toString());
			if (isAcademy)
				_ps.setBoolean(++param, true);
			_ps.setBoolean(++param, true);
			_ps.setInt(++param, Math.max(1, minExams));
			List<QuestionProfile> results = execute();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all active Questions linked to a particular Pilot Examination. 
	 * @param exam the ExamProfile bean
	 * @param isRandom randomly order Questions
	 * @return a List of QuestionProfiles
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if exam is null
	 */
	public List<QuestionProfile> getQuestionPool(ExamProfile exam, boolean isRandom) throws DAOException {
		return getQuestionPool(exam, isRandom, 0);
	}
	
	/**
	 * Loads all active Questions linked to a particular Pilot Examination. If a Pilot ID is specified, questions will be selected
	 * in increasing order of frequency of visibility in prior examination written by this Pilot. 
	 * @param exam the ExamProfile bean
	 * @param isRandom randomly order Questions
	 * @param pilotID the user's database ID, or zero if none
	 * @return a List of QuestionProfiles
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if exam is null
	 */
	public List<QuestionProfile> getQuestionPool(ExamProfile exam, boolean isRandom, int pilotID) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, "
			+ "QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A, COUNT(EQ.EXAM_ID)+(RAND()*3) AS CNT FROM exams.QE_INFO QE, "
			+ "exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS ON (Q.ID=QS.ID) LEFT JOIN exams.QUESTIONIMGS QI "
			+ "ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ "
			+ "ON (Q.ID=RQ.ID) LEFT JOIN exams.EXAMS E ON (E.PILOT_ID=?) LEFT JOIN exams.EXAMQUESTIONS EQ ON "
			+ "(EQ.QUESTION_ID=Q.ID) AND (EQ.EXAM_ID=E.ID) WHERE (Q.ID=QE.QUESTION_ID) AND (Q.ACTIVE=?) AND "
			+ "(QE.EXAM_NAME=?) GROUP BY Q.ID ");
		if (pilotID > 0)
			sqlBuf.append("ORDER BY CNT");
		else if (isRandom)
			sqlBuf.append("ORDER BY RAND()");
		sqlBuf.append(" LIMIT ?");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, pilotID);
			_ps.setBoolean(2, true);
			_ps.setString(3, exam.getName());
			_ps.setInt(4, exam.getSize());
			List<QuestionProfile> results = execute();
			loadMultiChoice(exam.getName(), results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load Question result sets.
	 */
	private List<QuestionProfile> execute() throws SQLException {
		List<QuestionProfile> results = new ArrayList<QuestionProfile>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				boolean isMultiChoice = (rs.getInt(8) > 0);
				boolean isRP = (rs.getString(14) != null);

				// Populate the Question Profile
				QuestionProfile qp = null;
				if (isRP) {
					RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
					rpqp.setAirportD(SystemData.getAirport(rs.getString(13)));
					rpqp.setAirportA(SystemData.getAirport(rs.getString(14)));	
					qp = rpqp;
				} else if (isMultiChoice)
					qp = new MultiChoiceQuestionProfile(rs.getString(2));
				else
					qp = new QuestionProfile(rs.getString(2));
			
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setActive(rs.getBoolean(4));
				qp.setOwner(SystemData.getApp(rs.getString(5)));
				qp.setTotalAnswers(rs.getInt(6));
				qp.setCorrectAnswers(rs.getInt(7));

				// Load image metadata
				if (rs.getInt(10) > 0) {
					qp.setType(rs.getInt(9));
					qp.setSize(rs.getInt(10));
					qp.setWidth(rs.getInt(11));
					qp.setHeight(rs.getInt(12));
				}
			
				results.add(qp);
			}
		}
		
		_ps.close();
		return results;
	}
	
	/*
	 * Helper method to populate multiple choice question options.
	 */
	private void loadMultiChoice(String examName, Collection<QuestionProfile> qs) throws SQLException {
		Map<Integer, QuestionProfile> resultMap = CollectionUtils.createMap(qs, QuestionProfile::getID);
		prepareStatementWithoutLimits("SELECT MQ.* FROM exams.QUESTIONMINFO MQ, exams.QUESTIONINFO Q, exams.EQ_INFO QE "
			+ "WHERE (Q.ID=QE.QUESTION_ID) AND (Q.ID=MQ.ID) AND (QE.EXAM_NAME=?) ORDER BY MQ.ID, MQ.SEQ");
		_ps.setString(1, examName);

		// Execute the query
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) resultMap.get(Integer.valueOf(rs.getInt(1)));
				if (mqp != null)
					mqp.addChoice(rs.getString(3));
			}
		}

		_ps.close();
	}
}
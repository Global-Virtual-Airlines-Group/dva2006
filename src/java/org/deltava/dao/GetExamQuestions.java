// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Examination questions.
 * @author Luke
 * @version 9.1
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
			QuestionProfile qp = null; boolean isMultiChoice = false;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONIMGS QI "
				+ "ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (MQ.ID=Q.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (RQ.ID=Q.ID) WHERE (Q.ID=?) GROUP BY Q.ID LIMIT 1")) {
				ps.setInt(1, id);

				// Execute the Query
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						isMultiChoice = (rs.getInt(7) > 0);
						boolean isImage = (rs.getInt(9) > 0);
						boolean isRP = (rs.getString(13) != null);

						// Populate the Question Profile
						if (isRP) {
							RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
							rpqp.setAirportD(SystemData.getAirport(rs.getString(12)));
							rpqp.setAirportA(SystemData.getAirport(rs.getString(13)));
							qp = rpqp;
						} else if (isMultiChoice)
							qp = new MultiChoiceQuestionProfile(rs.getString(2));
						else
							qp = new QuestionProfile(rs.getString(2));
			
						qp.setID(rs.getInt(1));
						qp.setCorrectAnswer(rs.getString(3));
						qp.setReference(rs.getString(4));
						qp.setActive(rs.getBoolean(5));
						qp.setOwner(SystemData.getApp(rs.getString(6)));

						// Load image data
						if (isImage) {
							qp.setType(rs.getInt(8));
							qp.setSize(rs.getInt(9));
							qp.setWidth(rs.getInt(10));
							qp.setHeight(rs.getInt(11));
						}
					}
				}
			}
			
			if (qp == null)
				return null;
			
			// Load stats
			try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(QS.TOTAL), SUM(QS.CORRECT) FROM exams.QUESTIONSTATS QS WHERE (QS.ID=?)")) {
				ps.setInt(1, qp.getID());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						qp.setTotalAnswers(rs.getInt(1));
						qp.setCorrectAnswers(rs.getInt(2));
					}
				}
			}

			// Load airlines
			try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRLINE FROM exams.QUESTIONAIRLINES WHERE (ID=?)")) {
				ps.setInt(1, qp.getID());
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						qp.addAirline(SystemData.getApp(rs.getString(1)));
				}
			}
			
			// Get multiple choice choices
			if (isMultiChoice) {
				MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) qp;
				try (PreparedStatement ps = prepareWithoutLimits("SELECT ANSWER FROM exams.QUESTIONMINFO WHERE (ID=?) ORDER BY SEQ")) {
					ps.setInt(1, qp.getID());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next())
							mqp.addChoice(rs.getString(1));	
					}
				}
			}
			
			// Get the exams for this question
			try (PreparedStatement ps = prepareWithoutLimits("SELECT QE.EXAM_NAME FROM exams.QE_INFO QE WHERE (QE.QUESTION_ID=?)")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						qp.addExam(rs.getString(1));	
				}
			}

			return qp;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns discrete pass / fail statistics for a particular Question.
	 * @param id the database ID
	 * @param isAcademy TRUE if Flight Academy only, otherwise FALSE
	 * @return a PassStatistics bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public PassStatistics getDiscreteStatistics(int id, boolean isAcademy) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(QS.TOTAL), SUM(QS.CORRECT) FROM exams.QUESTIONSTATS QS WHERE (QS.ID=?) AND (QS.ACADEMY=?)")) {
			ps.setInt(1,  id);
			ps.setBoolean(2, isAcademy);
			
			TestStatistics st = new TestStatistics();
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					st.setTotal(rs.getInt(1));
					st.setPassCount(rs.getInt(2));
				}
			}
			
			return st;
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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q "
			+ "LEFT JOIN exams.QUESTIONSTATS QS ON (Q.ID=QS.ID) LEFT JOIN exams.QE_INFO QE ON (Q.ID=QE.QUESTION_ID) LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) "
			+ "LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) ");
		if (exam != null)
			sqlBuf.append("WHERE (QE.EXAM_NAME=?) ");
		sqlBuf.append("GROUP BY Q.ID");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (exam != null)
				ps.setString(1, exam.getName());
			
			// Load results
			List<QuestionProfile> results = execute(ps);
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
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setBoolean(1, true);
			if (isAcademy)
				ps.setBoolean(2, true);
			
			return execute(ps);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A FROM exams.QUESTIONINFO Q "
			+ "LEFT JOIN exams.QUESTIONSTATS QS ON ((Q.ID=QS.ID)");
		if (isAcademy)
			sqlBuf.append(" AND (QS.ACADEMY=?)");
		sqlBuf.append(") LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) "
			+ "WHERE (Q.ACTIVE=?) GROUP BY Q.ID HAVING (QS.TOTAL>=?) ORDER BY (QS.CORRECT/QS.TOTAL)");
		if (isDesc)
			sqlBuf.append(" DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) { 
			int param = 0;
			if (isAcademy)
				ps.setBoolean(++param, true);
			ps.setBoolean(++param, true);
			ps.setInt(++param, Math.max(1, minExams));
			return execute(ps);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, QS.TOTAL, QS.CORRECT, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A, COUNT(EQ.EXAM_ID)+(RAND()*3) AS CNT "
			+ "FROM exams.QE_INFO QE, exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONSTATS QS ON (Q.ID=QS.ID) LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ "
			+ "ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) LEFT JOIN exams.EXAMS E ON (E.PILOT_ID=?) LEFT JOIN exams.EXAMQUESTIONS EQ ON (EQ.QUESTION_ID=Q.ID) AND "
			+ "(EQ.EXAM_ID=E.ID) WHERE (Q.ID=QE.QUESTION_ID) AND (Q.ACTIVE=?) AND (QE.EXAM_NAME=?) GROUP BY Q.ID ");
		if (pilotID > 0)
			sqlBuf.append("ORDER BY CNT");
		else if (isRandom)
			sqlBuf.append("ORDER BY RAND()");
		sqlBuf.append(" LIMIT ?");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setBoolean(2, true);
			ps.setString(3, exam.getName());
			ps.setInt(4, exam.getSize());
			List<QuestionProfile> results = execute(ps);
			loadMultiChoice(exam.getName(), results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load Question result sets.
	 */
	private static List<QuestionProfile> execute(PreparedStatement ps) throws SQLException {
		List<QuestionProfile> results = new ArrayList<QuestionProfile>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				boolean isMultiChoice = (rs.getInt(9) > 0);
				boolean isRP = (rs.getString(15) != null);

				// Populate the Question Profile
				QuestionProfile qp = null;
				if (isRP) {
					RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
					rpqp.setAirportD(SystemData.getAirport(rs.getString(14)));
					rpqp.setAirportA(SystemData.getAirport(rs.getString(15)));	
					qp = rpqp;
				} else if (isMultiChoice)
					qp = new MultiChoiceQuestionProfile(rs.getString(2));
				else
					qp = new QuestionProfile(rs.getString(2));
			
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setReference(rs.getString(4));
				qp.setActive(rs.getBoolean(5));
				qp.setOwner(SystemData.getApp(rs.getString(6)));
				qp.setTotalAnswers(rs.getInt(7));
				qp.setCorrectAnswers(rs.getInt(8));

				// Load image metadata
				if (rs.getInt(11) > 0) {
					qp.setType(rs.getInt(10));
					qp.setSize(rs.getInt(11));
					qp.setWidth(rs.getInt(12));
					qp.setHeight(rs.getInt(13));
				}
			
				results.add(qp);
			}
		}
		
		return results;
	}
	
	/*
	 * Helper method to populate multiple choice question options.
	 */
	private void loadMultiChoice(String examName, Collection<QuestionProfile> qs) throws SQLException {
		Map<Integer, QuestionProfile> resultMap = CollectionUtils.createMap(qs, QuestionProfile::getID);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT MQ.* FROM exams.QUESTIONMINFO MQ, exams.QUESTIONINFO Q, exams.QE_INFO QE WHERE (Q.ID=QE.QUESTION_ID) AND (Q.ID=MQ.ID) AND (QE.EXAM_NAME=?) ORDER BY MQ.ID, MQ.SEQ")) {
			ps.setString(1, examName);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) resultMap.get(Integer.valueOf(rs.getInt(1)));
					if (mqp != null)
						mqp.addChoice(rs.getString(3));
				}
			}
		}
	}
}
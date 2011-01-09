// Copyright 2005, 2006, 2007, 2008, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.cache.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Examination questions.
 * @author Luke
 * @version 3.5
 * @since 2.1
 */

public class GetExamQuestions extends DAO implements CachingDAO {
	
	private static final Cache<ExamResults> _rCache = new ExpiringCache<ExamResults>(120, 7200);

	private class ExamResults extends DatabaseBean {

		private int _total;
		private int _correct;

		ExamResults(int id, int total, int correct) {
			super();
			setID(id);
			_total = total;
			_correct = correct;
		}

		public int getTotal() {
			return _total;
		}

		public int getCorrect() {
			return _correct;
		}
	}

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamQuestions(Connection c) {
		super(c);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_rCache);
	}
	
	/**
	 * Loads a Question Profile.
	 * @param id the Question ID
	 * @return the Question profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public QuestionProfile getQuestionProfile(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A "
					+ "FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN "
					+ "exams.QUESTIONMINFO MQ ON (MQ.ID=Q.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (RQ.ID=Q.ID) "
					+ "WHERE (Q.ID=?) GROUP BY Q.ID LIMIT 1");
			_ps.setInt(1, id);

			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}
			
			// Check if we are multiple choice/have an image
			boolean isMultiChoice = (rs.getInt(6) > 0);
			boolean isImage = (rs.getInt(8) > 0);
			boolean isRP = (rs.getString(12) != null);

			// Populate the Question Profile
			QuestionProfile qp = null;
			if (isRP) {
				RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
				rpqp.setAirportD(SystemData.getAirport(rs.getString(11)));
				rpqp.setAirportA(SystemData.getAirport(rs.getString(12)));
				qp = rpqp;
			} else if (isMultiChoice)
				qp = new MultiChoiceQuestionProfile(rs.getString(2));
			else
				qp = new QuestionProfile(rs.getString(2));
			
			qp.setID(rs.getInt(1));
			qp.setCorrectAnswer(rs.getString(3));
			qp.setActive(rs.getBoolean(4));
			qp.setOwner(SystemData.getApp(rs.getString(5)));

			// Load image data
			if (isImage) {
				qp.setType(rs.getInt(7));
				qp.setSize(rs.getInt(8));
				qp.setWidth(rs.getInt(9));
				qp.setHeight(rs.getInt(10));
			}

			// Clean up
			rs.close();
			_ps.close();
			
			// Load airlines
			prepareStatementWithoutLimits("SELECT AIRLINE FROM exams.QUESTIONAIRLINES WHERE (ID=?)");
			_ps.setInt(1, qp.getID());
			rs = _ps.executeQuery();
			while (rs.next())
				qp.addAirline(SystemData.getApp(rs.getString(1)));
			
			// Clean up
			rs.close();
			_ps.close();

			// Load correct answer copunts
			loadResults(Collections.singleton(qp), false);

			// Get multiple choice choices
			if (isMultiChoice) {
				MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) qp;
				prepareStatementWithoutLimits("SELECT ANSWER FROM exams.QUESTIONMINFO WHERE (ID=?) ORDER BY SEQ");
				_ps.setInt(1, qp.getID());

				// Execute the Query
				rs = _ps.executeQuery();
				while (rs.next())
					mqp.addChoice(rs.getString(1));

				// Clean up
				rs.close();
				_ps.close();
			}
			
			// Get the exams for this question
			prepareStatementWithoutLimits("SELECT QE.EXAM_NAME FROM exams.QE_INFO QE WHERE (QE.QUESTION_ID=?)");
			_ps.setInt(1, id);

			// Populate the exam names
			rs = _ps.executeQuery();
			while (rs.next())
				qp.addExam(rs.getString(1));

			// Clean up and return
			rs.close();
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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D,"
				+ "RQ.AIRPORT_A FROM exams.QUESTIONINFO Q LEFT JOIN exams.QE_INFO QE ON (Q.ID=QE.QUESTION_ID) "
				+ "LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) "
				+ "LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) ");
		if (exam != null)
			sqlBuf.append("WHERE (QE.EXAM_NAME=?) ");
		sqlBuf.append("GROUP BY Q.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			if (exam != null)
				_ps.setString(1, exam.getName());
			
			// Load results
			List<QuestionProfile> results = execute();
			loadResults(results, false);
			if (exam != null)
				loadMultiChoice(exam.getName(), results);

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the most commonly asked active examination Questions.
	 * @return a List of QuestionProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getMostPopular() throws DAOException {
		try {
			prepareStatement("SELECT Q.*, COUNT(DISTINCT MQ.SEQ), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A, "
				+ "(SELECT COUNT(EQ.EXAM_ID) FROM exams.EXAMQUESTIONS EQ WHERE (EQ.QUESTION_ID=Q.ID) GROUP BY Q.ID) AS CNT, "
				+ "(SELECT SUM(EQ.CORRECT) FROM exams.EXAMQUESTIONS EQ WHERE (EQ.QUESTION_ID=Q.ID) GROUP BY Q.ID) AS SCOR "
				+ "FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ "
				+ "ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) WHERE (Q.ACTIVE=?) GROUP BY Q.ID "
				+ "ORDER BY CNT DESC");
			_ps.setBoolean(1, true);
			List<QuestionProfile> results = execute();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns active examination Questions based on the frequency of being answered correctly.
	 * @param isDesc TRUE if in descening order of correct answers, otherwise FALSE
	 * @param minExams the minimum number of exams the Question must have been in
	 * @return a List of QuestionProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getResults(boolean isDesc, int minExams) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(DISTINCT MQ.SEQ), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, "
			+ "RQ.AIRPORT_A, (SELECT COUNT(EQ.EXAM_ID) FROM exams.EXAMQUESTIONS EQ WHERE (EQ.QUESTION_ID=Q.ID) "
			+ "GROUP BY Q.ID) AS CNT, (SELECT SUM(EQ.CORRECT) FROM exams.EXAMQUESTIONS EQ WHERE (EQ.QUESTION_ID=Q.ID) "
			+ "GROUP BY Q.ID) AS CR, (SELECT AVG(EQ.CORRECT) FROM exams.EXAMQUESTIONS EQ WHERE (EQ.QUESTION_ID=Q.ID) "
			+ "GROUP BY Q.ID) AS SCOR FROM exams.QUESTIONINFO Q LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN "
			+ "exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) WHERE (Q.ACTIVE=?) "
			+ "GROUP BY Q.ID HAVING (CNT>=?) ORDER BY SCOR");
		if (isDesc)
			sqlBuf.append(" DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			_ps.setInt(2, minExams);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, "
			+ "RQ.AIRPORT_A, COUNT(EQ.EXAM_ID)+RAND() AS CNT FROM exams.QE_INFO QE, exams.QUESTIONINFO Q "
			+ "LEFT JOIN exams.QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN exams.QUESTIONMINFO MQ ON (Q.ID=MQ.ID) "
			+ "LEFT JOIN exams.QUESTIONRPINFO RQ ON (Q.ID=RQ.ID) LEFT JOIN exams.EXAMS E ON (E.PILOT_ID=?) LEFT JOIN "
			+ "exams.EXAMQUESTIONS EQ ON (EQ.QUESTION_ID=Q.ID) AND (EQ.EXAM_ID=E.ID) WHERE (Q.ID=QE.QUESTION_ID) "
			+ "AND (Q.ACTIVE=?) AND (QE.EXAM_NAME=?) GROUP BY Q.ID ");
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
			
			// Load the correct answer counts and multiple choice options
			loadResults(results, false);
			loadMultiChoice(exam.getName(), results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load Question result sets.
	 */
	private List<QuestionProfile> execute() throws SQLException {
		List<QuestionProfile> results = new ArrayList<QuestionProfile>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasStats = (rs.getMetaData().getColumnCount() > 13);
		while (rs.next()) {
			// Check if we are multiple choice
			boolean isMultiChoice = (rs.getInt(6) > 0);
			boolean isRP = (rs.getString(12) != null);

			// Populate the Question Profile
			QuestionProfile qp = null;
			if (isRP) {
				RoutePlotQuestionProfile rpqp = new RoutePlotQuestionProfile(rs.getString(2));
				rpqp.setAirportD(SystemData.getAirport(rs.getString(11)));
				rpqp.setAirportA(SystemData.getAirport(rs.getString(12)));	
				qp = rpqp;
			} else if (isMultiChoice)
				qp = new MultiChoiceQuestionProfile(rs.getString(2));
			else
				qp = new QuestionProfile(rs.getString(2));
			
			qp.setID(rs.getInt(1));
			qp.setCorrectAnswer(rs.getString(3));
			qp.setActive(rs.getBoolean(4));
			qp.setOwner(SystemData.getApp(rs.getString(5)));

			// Load image metadata
			if (rs.getInt(8) > 0) {
				qp.setType(rs.getInt(7));
				qp.setSize(rs.getInt(8));
				qp.setWidth(rs.getInt(9));
				qp.setHeight(rs.getInt(10));
			}
			
			// Load stats
			if (hasStats) {
				qp.setTotalAnswers(rs.getInt(13));
				qp.setCorrectAnswers(rs.getInt(14));
				ExamResults er = new ExamResults(qp.getID(), qp.getTotalAnswers(), qp.getCorrectAnswers());
				_rCache.add(er);
			}

			// Add to results
			results.add(qp);
		}
		
		// clean up
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to populate multiple choice question options.
	 */
	private void loadMultiChoice(String examName, Collection<QuestionProfile> qs) throws SQLException {
		Map<Integer, QuestionProfile> resultMap = CollectionUtils.createMap(qs, "ID");
		prepareStatementWithoutLimits("SELECT MQ.* FROM exams.QUESTIONMINFO MQ, exams.QUESTIONINFO Q, "
				+ "exams.QE_INFO QE WHERE (Q.ID=QE.QUESTION_ID) AND (Q.ID=MQ.ID) AND (QE.EXAM_NAME=?) "
				+ "ORDER BY MQ.ID, MQ.SEQ");
		_ps.setString(1, examName);

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) resultMap.get(new Integer(rs.getInt(1)));
			if (mqp != null)
				mqp.addChoice(rs.getString(3));
		}

		// Clean up
		rs.close();
		_ps.close();
	}
	
	/**
	 * Helper method to populate correct/incorrect answer statistics.
	 */
	private void loadResults(Collection<QuestionProfile> qs, boolean loadAll) throws SQLException {

		// Determine what question IDs to load
		Collection<Integer> IDs = new HashSet<Integer>();
		for (Iterator<QuestionProfile> i = qs.iterator(); i.hasNext();) {
			QuestionProfile qp = i.next();
			Integer id = new Integer(qp.getID());
			ExamResults er = _rCache.get(id);
			if (er == null)
				IDs.add(id);
			else {
				qp.setTotalAnswers(er.getTotal());
				qp.setCorrectAnswers(er.getCorrect());
			}
		}

		// Do nothing if empty collection
		if (IDs.isEmpty())
			return;

		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.QUESTION_ID, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM "
				+ "exams.EXAMQUESTIONS EQ, exams.EXAMS E WHERE (EQ.EXAM_ID=E.ID) AND (E.CREATED_ON >= "
				+ "DATE_SUB(NOW(), INTERVAL ? DAY)) AND (E.ISEMPTY=?) AND (EQ.QUESTION_ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext();) {
			sqlBuf.append(i.next().toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append(")) GROUP BY EQ.QUESTION_ID");

		// Prepare the statement
		prepareStatementWithoutLimits(sqlBuf.toString());
		_ps.setInt(1, loadAll ? Short.MAX_VALUE : SystemData.getInt("testing.correct_ratio_age", 90));
		_ps.setBoolean(2, false);

		// Execute the query and populate the cache and question profiles
		ResultSet rs = _ps.executeQuery();
		Map<Integer, QuestionProfile> questions = CollectionUtils.createMap(qs, "ID");
		while (rs.next()) {
			ExamResults er = new ExamResults(rs.getInt(1), rs.getInt(2), rs.getInt(3));
			_rCache.add(er);
			QuestionProfile qp = questions.get(er.cacheKey());
			if (qp != null) {
				qp.setTotalAnswers(er.getTotal());
				qp.setCorrectAnswers(er.getCorrect());
			}
		}

		// Clean up after ourselves
		rs.close();
		_ps.close();
	}
}
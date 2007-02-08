// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read examination configuration data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetExamProfiles extends DAO {
	
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
	public GetExamProfiles(Connection c) {
		super(c);
	}

	/**
	 * Loads an Examination profile.
	 * @param examName the examination name
	 * @return an ExamProfile bean, or null if the exam was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ExamProfile getExamProfile(String examName) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM EXAMINFO WHERE (NAME=?)");
			_ps.setString(1, examName);

			// Execute the query - return null if not found
			List<ExamProfile> results = execute();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Examination profiles.
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getExamProfiles() throws DAOException {
		try {
			prepareStatement("SELECT * FROM EXAMINFO ORDER BY STAGE, NAME");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Examination profiles included within the Flight Academy or Testing Center.
	 * @param isAcademy TRUE if Flight Academy exams should be returned, otherwise FALSE for the Testing Center
	 * @return a List of ExamProfile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ExamProfile> getExamProfiles(boolean isAcademy) throws DAOException {
		try {
			prepareStatement("SELECT * FROM EXAMINFO WHERE (ACADEMY=?) ORDER BY STAGE, NAME");
			_ps.setBoolean(1, isAcademy);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads a Question Profile.
	 * @param id the Question ID
	 * @return the Question profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public QuestionProfile getQuestionProfile(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y FROM QUESTIONINFO Q LEFT JOIN "
					+ "QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN QUESTIONMINFO MQ ON (MQ.ID=Q.ID) WHERE (Q.ID=?) "
					+ "GROUP BY Q.ID");
			_ps.setInt(1, id);

			// Execute the Query
			setQueryMax(0);
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}

			// Check if we are multiple choice
			boolean isMultiChoice = (rs.getInt(5) > 0);

			// Populate the Question Profile
			QuestionProfile qp = isMultiChoice ? new MultiChoiceQuestionProfile(rs.getString(2)) : new QuestionProfile(
					rs.getString(2));
			qp.setID(rs.getInt(1));
			qp.setCorrectAnswer(rs.getString(3));
			qp.setActive(rs.getBoolean(4));
			// qp.setTotalAnswers(rs.getInt(5));
			//qp.setCorrectAnswers(rs.getInt(6));
			
			// Load image data
			if (rs.getInt(7) > 0) {
				qp.setType(rs.getInt(6));
				qp.setSize(rs.getInt(7));
				qp.setWidth(rs.getInt(8));
				qp.setHeight(rs.getInt(9));
			}

			// Clean up
			rs.close();
			_ps.close();
			
			// Load correct answer copunts
			loadResults(Collections.singleton(qp));

			// Get multiple choice choices
			if (isMultiChoice) {
				MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) qp;
				prepareStatementWithoutLimits("SELECT ANSWER FROM QUESTIONMINFO WHERE (ID=?) ORDER BY SEQ");
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
			prepareStatementWithoutLimits("SELECT EXAM_NAME FROM QE_INFO WHERE (QUESTION_ID=?)");
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
	 * Loads all Questions linked to a particular Pilot Examination.
	 * @param examName the Examination name
	 * @param isRandom randomly order Questions
	 * @param isActive only include active Questions
	 * @return a List of QuestionProfiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<QuestionProfile> getQuestionPool(String examName, boolean isRandom, boolean isActive) throws DAOException {

		// Check if we're displaying all questions
		boolean showAll = "ALL".equalsIgnoreCase(examName);
		isActive |= showAll;
		
		// Build conditions
		Collection<String> conditions = new ArrayList<String>();
		if (isActive) conditions.add("(Q.ACTIVE=?)");
		if (!showAll) conditions.add("(QE.EXAM_NAME=?)");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(MQ.ID), QI.TYPE, QI.SIZE, QI.X, QI.Y FROM QUESTIONINFO Q "
				+ "LEFT JOIN QE_INFO QE ON (Q.ID=QE.QUESTION_ID) LEFT JOIN QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN "
				+ "QUESTIONMINFO MQ ON (Q.ID=MQ.ID) ");

		// Append conditions
		if (!conditions.isEmpty()) sqlBuf.append("WHERE ");
		for (Iterator<String> i = conditions.iterator(); i.hasNext(); ) {
			sqlBuf.append(i.next());
			if (i.hasNext())
				sqlBuf.append(" AND ");
		}

		// Add GROUP/ORDER BY
		sqlBuf.append(" GROUP BY Q.ID");
		if (isRandom)
			sqlBuf.append(" ORDER BY RAND()");

		try {
			prepareStatement(sqlBuf.toString());
			
			// Set the parameters
			int pNum = 0;
			if (isActive)
				_ps.setBoolean(++pNum, isActive);
			if (!showAll)
				_ps.setString(++pNum, examName);

			// Execute the Query
			boolean hasMultiChoice = false;
			List<QuestionProfile> results = new ArrayList<QuestionProfile>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				// Check if we are multiple choice
				boolean isMultiChoice = (rs.getInt(5) > 0);
				hasMultiChoice |= isMultiChoice;
				
				// Populate the Question Profile
				QuestionProfile qp = isMultiChoice ? new MultiChoiceQuestionProfile(rs.getString(2)) : new QuestionProfile(
						rs.getString(2));
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setActive(rs.getBoolean(4));
				
				// Load image metadata
				if (rs.getInt(7) > 0) {
					qp.setType(rs.getInt(6));
					qp.setSize(rs.getInt(7));
					qp.setWidth(rs.getInt(8));
					qp.setHeight(rs.getInt(9));
				}

				// Add to results
				results.add(qp);
			}

			// Clean up
			rs.close();
			_ps.close();
			
			// Load the correct answer counts
			loadResults(results);
			
			// Convert to a map so we can load multiple choices
			if (hasMultiChoice) {
				Map<Integer, QuestionProfile> resultMap = CollectionUtils.createMap(results, "ID");
				
				// Build the SQL statement
				sqlBuf = new StringBuilder("SELECT MQ.* FROM QUESTIONMINFO MQ, QUESTIONINFO Q, QE_INFO QE WHERE "
						+ "(Q.ID=QE.QUESTION_ID) AND (Q.ID=MQ.ID)");
				if (!showAll)
					sqlBuf.append(" AND (QE.EXAM_NAME=?)");
				
				sqlBuf.append(" ORDER BY MQ.ID, MQ.SEQ");
				
				// Prepare the query
				prepareStatementWithoutLimits(sqlBuf.toString());
				if (!showAll)
					_ps.setString(1, examName);
				
				// Execute the query
				rs = _ps.executeQuery();
				while (rs.next()) {
					MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) resultMap.get(new Integer(rs.getInt(1)));
					if (mqp != null)
						mqp.addChoice(rs.getString(3));
				}
				
				// Clean up
				rs.close();
				_ps.close();
			}
			
			// Return results
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads a Check Ride script.
	 * @param eqType the equipment type
	 * @return a CheckRideScript bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRideScript getScript(String eqType) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM CR_DESCS WHERE (EQTYPE=?)");
			_ps.setString(1, eqType);

			// Execute the Query - return null if nothing found
			ResultSet rs = _ps.executeQuery();
			setQueryMax(0);
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}

			// Create the bean
			CheckRideScript result = new CheckRideScript(rs.getString(1));
			result.setProgram(rs.getString(2));
			result.setDescription(rs.getString(3));

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Check Ride scripts.
	 * @return a List of CheckRideScript beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRideScript> getScripts() throws DAOException {
		try {
			prepareStatement("SELECT * FROM CR_DESCS");

			// Execute the query
			List<CheckRideScript> results = new ArrayList<CheckRideScript>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				CheckRideScript sc = new CheckRideScript(rs.getString(1));
				sc.setProgram(rs.getString(2));
				sc.setDescription(rs.getString(3));
				results.add(sc);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse ExamProfile result sets.
	 */
	private List<ExamProfile> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<ExamProfile> results = new ArrayList<ExamProfile>();
		while (rs.next()) {
			ExamProfile ep = new ExamProfile(rs.getString(1));
			ep.setStage(rs.getInt(2));
			ep.setMinStage(rs.getInt(3));
			ep.setEquipmentType(rs.getString(4));
			ep.setSize(rs.getInt(5));
			ep.setPassScore(rs.getInt(6));
			ep.setTime(rs.getInt(7));
			ep.setActive(rs.getBoolean(8));
			ep.setAcademy(rs.getBoolean(9));

			// Add to results
			results.add(ep);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to populate correct/incorrect answer statistics.
	 */
	private void loadResults(Collection<QuestionProfile> qs) throws SQLException {

		// Determine what question IDs to load
		Collection<Integer> IDs = new HashSet<Integer>();
		for (Iterator<QuestionProfile> i = qs.iterator(); i.hasNext(); ) {
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
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.QUESTION_ID, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM "
				+ "EXAMQUESTIONS EQ, EXAMS E WHERE (EQ.EXAM_ID=E.ID) AND (E.CREATED_ON >= DATE_SUB(NOW(), "
				+ "INTERVAL ? DAY)) AND (E.ISEMPTY=?) AND (EQ.QUESTION_ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			sqlBuf.append(i.next().toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) GROUP BY EQ.QUESTION_ID");
		
		// Prepare the statement
		prepareStatement(sqlBuf.toString());
		_ps.setInt(1, SystemData.getInt("testing.correct_ratio_age", 90));
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
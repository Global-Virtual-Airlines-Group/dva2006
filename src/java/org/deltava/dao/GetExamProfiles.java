// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read examination configuration data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetExamProfiles extends DAO {

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
			prepareStatement("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT), COUNT(MQ.ID), QI.TYPE, QI.SIZE, "
					+ "QI.X, QI.Y FROM QUESTIONINFO Q LEFT JOIN EXAMQUESTIONS EQ ON (Q.ID=EQ.QUESTION_ID) LEFT JOIN "
					+ "QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN EXAMS E ON (EQ.EXAM_ID=E.ID) AND (E.ISEMPTY=?) AND "
					+ "(E.CREATED_ON >= DATE_SUB(NOW(), INTERVAL ? DAY)) LEFT JOIN QUESTIONMINFO MQ ON (MQ.ID=Q.ID) "
					+ "WHERE (Q.ID=?) GROUP BY Q.ID");
			_ps.setBoolean(1, false);
			_ps.setInt(2, SystemData.getInt("testing.correct_ratio_age", 90));
			_ps.setInt(3, id);

			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}

			// Check if we are multiple choice
			boolean isMultiChoice = (rs.getInt(7) > 0);

			// Populate the Question Profile
			QuestionProfile qp = isMultiChoice ? new MultiChoiceQuestionProfile(rs.getString(2)) : new QuestionProfile(
					rs.getString(2));
			qp.setID(rs.getInt(1));
			qp.setCorrectAnswer(rs.getString(3));
			qp.setActive(rs.getBoolean(4));
			qp.setTotalAnswers(rs.getInt(5));
			qp.setCorrectAnswers(rs.getInt(6));
			
			// Load image data
			if (rs.getInt(9) > 0) {
				qp.setType(rs.getInt(8));
				qp.setSize(rs.getInt(9));
				qp.setWidth(rs.getInt(10));
				qp.setHeight(rs.getInt(11));
			}

			// Clean up
			rs.close();
			_ps.close();

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
		StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT), COUNT(MQ.ID), QI.TYPE, "
				+ "QI.SIZE, QI.X, QI.Y FROM QUESTIONINFO Q LEFT JOIN EXAMQUESTIONS EQ ON (Q.ID=EQ.QUESTION_ID) LEFT JOIN "
				+ "QE_INFO QE ON (Q.ID=QE.QUESTION_ID) LEFT JOIN QUESTIONIMGS QI ON (Q.ID=QI.ID) LEFT JOIN EXAMS E ON "
				+ "(EQ.EXAM_ID=E.ID) AND (E.ISEMPTY=?) AND (E.CREATED_ON >= DATE_SUB(NOW(), INTERVAL ? DAY)) LEFT JOIN "
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
			_ps.setBoolean(++pNum, false);
			_ps.setInt(++pNum, SystemData.getInt("testing.correct_ratio_age", 90));
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
				boolean isMultiChoice = (rs.getInt(7) > 0);
				hasMultiChoice |= isMultiChoice;
				
				// Populate the Question Profile
				QuestionProfile qp = isMultiChoice ? new MultiChoiceQuestionProfile(rs.getString(2)) : new QuestionProfile(
						rs.getString(2));
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setActive(rs.getBoolean(4));
				qp.setTotalAnswers(rs.getInt(5));
				qp.setCorrectAnswers(rs.getInt(6));
				
				// Load image metadata
				if (rs.getInt(9) > 0) {
					qp.setType(rs.getInt(8));
					qp.setSize(rs.getInt(9));
					qp.setWidth(rs.getInt(10));
					qp.setHeight(rs.getInt(11));
				}

				// Add to results
				results.add(qp);
			}

			// Clean up
			rs.close();
			_ps.close();
			
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
}
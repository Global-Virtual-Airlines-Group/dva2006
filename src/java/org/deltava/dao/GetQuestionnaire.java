// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Applicant Questionaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetQuestionnaire extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetQuestionnaire(Connection c) {
		super(c);
	}

	/**
	 * Retrieves a particular Applicant Questionnaire.
	 * @param id the Questionare database ID
	 * @return an Examination bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Examination get(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_ID), SUM(Q.CORRECT) FROM APPEXAMS E, "
					+ "APPQUESTIONS Q WHERE (E.ID=Q.EXAM_ID) AND (E.ID=?) GROUP BY E.ID");
			_ps.setInt(1, id);

			// Get the examination
			List results = execute();
			setQueryMax(0);
			if (results.isEmpty())
				return null;

			// Get the exam bean
			Examination e = (Examination) results.get(0);

			// Load the questions for this examination
			prepareStatementWithoutLimits("SELECT EQ.*, COUNT(MQ.SEQ), QI.TYPE, QI.SIZE, QI.X, QI.Y FROM "
					+ "APPQUESTIONS EQ LEFT JOIN APPQUESTIONSM MQ ON (EQ.EXAM_ID=MQ.EXAM_ID) AND "
					+ "(EQ.QUESTION_ID=MQ.QUESTION_ID) LEFT JOIN exams.QUESTIONIMGS QI ON (EQ.QUESTION_ID=QI.ID) "
					+ "WHERE (EQ.EXAM_ID=?) GROUP BY EQ.QUESTION_ID, EQ.QUESTION_NO ORDER BY EQ.QUESTION_NO");
			_ps.setInt(1, id);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				boolean isMC = (rs.getInt(8) > 0);
				Question q = isMC ? new MultiChoiceQuestion(rs.getString(4)) : new Question(rs.getString(4));
				q.setID(rs.getInt(2));
				q.setCorrectAnswer(rs.getString(5));
				q.setAnswer(rs.getString(6));
				q.setCorrect(rs.getBoolean(7));
				if (rs.getInt(10) > 0) {
					q.setType(rs.getInt(9));
					q.setSize(rs.getInt(10));
					q.setWidth(rs.getInt(11));
					q.setHeight(rs.getInt(12));
				}

				// Add question to the examination
				e.addQuestion(q);
			}

			// Clean up
			rs.close();
			_ps.close();
			
			// Load multiple choice questions
			if (e.hasMultipleChoice()) {
				Map<Integer, Question> qMap = CollectionUtils.createMap(e.getQuestions(), "ID");
				prepareStatementWithoutLimits("SELECT QUESTION_ID, SEQ, ANSWER FROM APPQUESTIONSM WHERE "
						+ "(EXAM_ID=?) ORDER BY QUESTION_ID, SEQ");
				_ps.setInt(1, e.getID());

				// Execute the query
				rs = _ps.executeQuery();
				while (rs.next()) {
					Question q = qMap.get(new Integer(rs.getInt(1)));
					if (q != null) {
						MultiChoiceQuestion mq = (MultiChoiceQuestion) q;
						mq.addChoice(rs.getString(3));
					}
				}

				// Clean up
				rs.close();
				_ps.close();
			}
			
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves a particular Applicant Questionnaire from an Applicant.
	 * @param applicantID the Applicant database ID
	 * @return an Examination bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Examination getByApplicantID(int applicantID) throws DAOException {

		int examID = 0;
		try {
			setQueryMax(1);
			prepareStatement("SELECT ID FROM APPEXAMS WHERE (APP_ID=?)");
			_ps.setInt(1, applicantID);

			// Get the ID
			ResultSet rs = _ps.executeQuery();
			examID = (rs.next()) ? rs.getInt(1) : 0;
			setQueryMax(0);

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// If we got the exam ID, get it, otherwise return null
		return (examID == 0) ? null : get(examID);
	}

	/**
	 * Returns all Submitted Applicant Questionnaires.
	 * @return a List of Examinations
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Examination> getPending() throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_ID), SUM(Q.CORRECT), A.FIRSTNAME, "
					+ "A.LASTNAME FROM APPEXAMS E, APPQUESTIONS Q, APPLICANTS A WHERE (E.ID=Q.EXAM_ID) AND "
					+ "(E.APP_ID=A.ID) AND (E.STATUS=?) GROUP BY E.ID ORDER BY E.CREATED_ON");
			_ps.setInt(1, Test.SUBMITTED);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Questionnaires for a subset of Applicants. 
	 * @param ids a Collection of Applicant databsae IDs
	 * @return a Map of Questionnaires, keyed by Applicant database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Examination> getByID(Collection<Integer> ids) throws DAOException {
		if (CollectionUtils.isEmpty(ids))
			return Collections.emptyMap();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT E.*, COUNT(DISTINCT Q.QUESTION_ID), SUM(Q.CORRECT), "
				+ "A.FIRSTNAME, A.LASTNAME FROM APPEXAMS E, APPQUESTIONS Q, APPLICANTS A WHERE (E.ID=Q.EXAM_ID) "
				+ "AND (E.APP_ID=A.ID) AND (A.ID IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) GROUP BY E.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			return CollectionUtils.createMap(execute(), "pilotID");
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<Examination> execute() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();
		boolean hasName = (rs.getMetaData().getColumnCount() > 13);

		// Iterate through the result set
		List<Examination> results = new ArrayList<Examination>();
		while (rs.next()) {
			Examination e = new Examination(SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME);
			e.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			e.setID(rs.getInt(1));
			e.setPilotID(rs.getInt(2));
			e.setStatus(rs.getInt(3));
			e.setDate(rs.getTimestamp(4));
			e.setExpiryDate(rs.getTimestamp(5));
			e.setSubmittedOn(rs.getTimestamp(6));
			e.setScoredOn(rs.getTimestamp(7));
			e.setScorerID(rs.getInt(8));
			e.setAutoScored(rs.getBoolean(9));
			e.setComments(rs.getString(10));
			e.setSize(rs.getInt(11));
			e.setScore(rs.getInt(12));

			// Add name data if present
			if (hasName) {
				e.setFirstName(rs.getString(13));
				e.setLastName(rs.getString(14));
			}

			// Add to results
			results.add(e);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
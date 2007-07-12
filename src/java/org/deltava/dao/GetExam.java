// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;
import org.deltava.comparators.TestComparator;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Acces Object for loading Examination/Check Ride data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetExam extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExam(Connection c) {
		super(c);
	}

	/**
	 * Loads a Pilot Examination.
	 * @param id the Database ID
	 * @return the Examination, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Examination getExam(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE, EP.ACADEMY "
					+ "FROM EXAMS E, EXAMQUESTIONS Q, EXAMINFO EP WHERE (E.ID=?) AND (E.NAME=EP.NAME) AND "
					+ "(E.ID=Q.EXAM_ID) GROUP BY E.ID");
			_ps.setInt(1, id);

			// Execute the query, return null if nothing
			List<Examination> results = execute();
			setQueryMax(0);
			if (results.isEmpty())
				return null;

			// Get the examination
			Examination e = results.get(0);

			// Load the questions for this examination
			prepareStatementWithoutLimits("SELECT EQ.*, COUNT(MQ.SEQ), QI.TYPE, QI.SIZE, QI.X, QI.Y FROM "
					+ "EXAMQUESTIONS EQ LEFT JOIN EXAMQUESTIONSM MQ ON (EQ.EXAM_ID=MQ.EXAM_ID) AND "
					+ "(EQ.QUESTION_ID=MQ.QUESTION_ID) LEFT JOIN QUESTIONIMGS QI ON (EQ.QUESTION_ID=QI.ID) "
					+ "WHERE (EQ.EXAM_ID=?) GROUP BY EQ.QUESTION_ID, EQ.QUESTION_NO ORDER BY EQ.QUESTION_NO");
			_ps.setInt(1, id);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				boolean isMC = (rs.getInt(8) > 0);

				// Create the question
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
				prepareStatementWithoutLimits("SELECT QUESTION_ID, SEQ, ANSWER FROM EXAMQUESTIONSM WHERE "
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
	 * Loads a Pilot Check Ride.
	 * @param id the Database ID
	 * @return the CheckRide, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRide getCheckRide(int id) throws DAOException {
		try {
			setQueryMax(1);
			if (SystemData.getBoolean("academy.enabled"))
				prepareStatement("SELECT CR.*, EQ.STAGE, CRR.COURSE FROM (CHECKRIDES CR, EQTYPES EQ) LEFT JOIN "
						+ "COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.ID=?)");
			else
				prepareStatement("SELECT CR.*, EQ.STAGE FROM CHECKRIDES CR, EQTYPES EQ WHERE (CR.EQTYPE=EQ.EQTYPE) "
						+ "AND (CR.ID=?)");	

			// Execute the query
			_ps.setInt(1, id);
			List results = executeCheckride();
			setQueryMax(0);
			return results.isEmpty() ? null : (CheckRide) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads a Pilot Check Ride associated with a particular ACARS Flight ID.
	 * @param acarsID the ACARS flight ID
	 * @return a CheckRide, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRide getACARSCheckRide(int acarsID) throws DAOException {
		try {
			setQueryMax(1);
			if (SystemData.getBoolean("academy.enabled"))
				prepareStatement("SELECT CR.*, EQ.STAGE, CRR.COURSE FROM (CHECKRIDES CR, EQTYPES EQ) LEFT JOIN "
						+ "COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.ACARS_ID=?)");
			else
				prepareStatement("SELECT CR.*, EQ.STAGE FROM CHECKRIDES CR, EQTYPES EQ WHERE "
						+ "(CR.EQTYPE=EQ.EQTYPE) AND (CR.ACARS_ID=?)");
			
			// Execute the query
			_ps.setInt(1, acarsID);
			List results = executeCheckride();
			setQueryMax(0);
			return results.isEmpty() ? null : (CheckRide) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads a pending Pilot Check Ride for a particular equipment type.
	 * @param dbName the database name
	 * @param pilotID the Pilot Database ID
	 * @param eqType the equipment type used
	 * @param status the CheckRide status
	 * @return a CheckRide, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRide getCheckRide(String dbName, int pilotID, String eqType, int status) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT CR.*, EQ.STAGE FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".CHECKRIDES CR, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".EQTYPES EQ WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.PILOT_ID=?) AND (CR.ACTYPE=?) "
				+ "AND (CR.STATUS=?)");

		try {
			setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, pilotID);
			_ps.setString(2, eqType);
			_ps.setInt(3, status);

			// Execute the query
			List<CheckRide> results = executeCheckride();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Check Rides for a particular Pilot.
	 * @param pilotID the pilot Database ID
	 * @return a Collection of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CheckRide> getCheckRides(int pilotID) throws DAOException {
		try {
			if (SystemData.getBoolean("academy.enabled"))
				prepareStatement("SELECT CR.*, EQ.STAGE, CRR.COURSE FROM (CHECKRIDES CR, EQTYPES EQ) LEFT JOIN "
						+ "COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.PILOT_ID=?) "
						+ "ORDER BY CR.CREATED");
			else
				prepareStatement("SELECT CR.*, EQ.STAGE FROM CHECKRIDES CR, EQTYPES EQ WHERE (CR.EQTYPE=EQ.EQTYPE) "
					+ " AND (CR.PILOT_ID=?) ORDER BY CR.CREATED");
			_ps.setInt(1, pilotID);
			return executeCheckride();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all submitted Check Rides.
	 * @param isAcademy TRUE if listing Flight Academy Check Rides, otherwise FALSE
	 * @return a Collection of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CheckRide> getCheckRideQueue(boolean isAcademy) throws DAOException {
		try {
			if (isAcademy) {
				prepareStatement("SELECT CR.*, EQ.STAGE, CRR.COURSE FROM (CHECKRIDES CR, EQTYPES EQ) LEFT JOIN "
						+ "COURSERIDES CRR ON (CRR.CHECKRIDE=CR.ID) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.STATUS=?) "
						+ "AND (CR.ACADEMY=?) ORDER BY CR.CREATED");
			} else {
				prepareStatement("SELECT CR.*, EQ.STAGE FROM CHECKRIDES CR, EQTYPES EQ WHERE (CR.EQTYPE=EQ.EQTYPE) "
						+ " AND (CR.STATUS=?) AND (CR.ACADEMY=?) ORDER BY CR.CREATED");
			}
			
			// Execute the query
			_ps.setInt(1, Test.SUBMITTED);
			_ps.setBoolean(2, isAcademy);
			return executeCheckride();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all examinations and check rides for a particular Pilot.
	 * @param id the Pilot's Database ID
	 * @return a List of Tests
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Test> getExams(int id) throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE, EP.ACADEMY "
					+ "FROM EXAMS E, EXAMQUESTIONS Q, EXAMINFO EP WHERE (E.PILOT_ID=?) AND (EP.NAME=E.NAME) "
					+ "AND (E.ID=Q.EXAM_ID) GROUP BY E.ID");
			_ps.setInt(1, id);

			// Execute the query
			List<Test> results = new ArrayList<Test>(execute());

			// Load Check Rides
			if (SystemData.getBoolean("academy.enabled"))
				prepareStatement("SELECT CR.*, EQ.STAGE, CRR.COURSE FROM (CHECKRIDES CR, EQTYPES EQ) LEFT JOIN "
						+ "COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.PILOT_ID=?)");
			else
				prepareStatement("SELECT CR.*, EQ.STAGE FROM CHECKRIDES CR, EQTYPES EQ WHERE (CR.EQTYPE=EQ.EQTYPE) "
						+ "AND (CR.PILOT_ID=?)");
			
			// Execute the query
			_ps.setInt(1, id);
			results.addAll(executeCheckride());

			// Sort the results to merge them in by date
			Collections.sort(results, new TestComparator(TestComparator.DATE));
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns automatically scored Examiantions.
	 * @param examName the Examination name, or null if all requested
	 * @return a Collection of Examination beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Examination> getAutoScored(String examName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), "
				+ "EP.STAGE, EP.ACADEMY, P.FIRSTNAME, P.LASTNAME FROM EXAMS E, EXAMQUESTIONS Q, PILOTS P, "
				+ "EXAMINFO EP WHERE (P.ID=E.PILOT_ID) AND (E.NAME=EP.NAME) AND (E.AUTOSCORE=?) AND "
				+ "(E.ID=Q.EXAM_ID) ");
		if (examName != null)
			sqlBuf.append("AND (E.NAME=?) ");
		
		sqlBuf.append("GROUP BY E.ID ORDER BY E.CREATED_ON DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			if (examName != null)
				_ps.setString(2, examName);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the names of all automatically scored examinations.
	 * @return a Collection of Examination names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getAutoScoredExamNames() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT NAME FROM EXAMS WHERE (AUTOSCORE=?)");
			_ps.setBoolean(1, true);
			
			// Do the query
			Collection<String> results = new TreeSet<String>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(rs.getString(1));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	} 
	
	/**
	 * Returns all Initial Questionnaires for hired Pilots.
	 * @param ids a Collection of Pilot database IDs
	 * @return a Map of Examination beans keyed by Pilot ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Examination> getQuestionnaires(Collection<Integer> ids) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), "
				+ "EP.STAGE, EP.ACADEMY FROM EXAMS E, EXAMQUESTIONS Q, EXAMINFO EP WHERE (E.NAME=?) AND "
				+ "(EP.NAME=E.NAME) AND (E.ID=Q.EXAM_ID) AND (E.ID IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) GROUP BY E.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, Examination.QUESTIONNAIRE_NAME);
			return CollectionUtils.createMap(execute(), "pilotID");
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all submitted Examinations.
	 * @return a List of Examinations
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Examination> getSubmitted() throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE, "
					+ "EP.ACADEMY, P.FIRSTNAME, P.LASTNAME FROM EXAMS E, EXAMQUESTIONS Q, PILOTS P, "
					+ "EXAMINFO EP WHERE (P.ID=E.PILOT_ID) AND (E.NAME=EP.NAME) AND ((E.STATUS=?) OR "
					+ "((E.STATUS=?) AND (E.EXPIRY_TIME < NOW()))) AND (E.ID=Q.EXAM_ID) GROUP BY E.ID "
					+ "ORDER BY E.CREATED_ON");
			_ps.setInt(1, Test.SUBMITTED);
			_ps.setInt(2, Test.NEW);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks if a Pilot has an Examination open or awaiting scoring.
	 * @param id the Pilot's database ID
	 * @return TRUE if an Examination is in NEW or SUBMITTED status
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getActiveExam(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT ID FROM EXAMS WHERE (PILOT_ID=?) AND ((STATUS=?) OR (STATUS=?))");
			_ps.setInt(1, id);
			_ps.setInt(2, Test.NEW);
			_ps.setInt(3, Test.SUBMITTED);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int testID = rs.next() ? rs.getInt(1) : 0;
			setQueryMax(0);

			// Clean up and return
			rs.close();
			_ps.close();
			return testID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse the examination result set.
	 */
	private List<Examination> execute() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();
		boolean hasName = (rs.getMetaData().getColumnCount() > 18);

		// Iterate through the results
		List<Examination> results = new ArrayList<Examination>();
		while (rs.next()) {
			Examination e = new Examination(rs.getString(2));
			e.setID(rs.getInt(1));
			e.setPilotID(rs.getInt(3));
			e.setStatus(rs.getInt(4));
			e.setDate(rs.getTimestamp(5));
			e.setExpiryDate(rs.getTimestamp(6));
			e.setSubmittedOn(rs.getTimestamp(7));
			e.setScoredOn(rs.getTimestamp(8));
			e.setScorerID(rs.getInt(9));
			e.setPassFail(rs.getBoolean(10));
			e.setEmpty(rs.getBoolean(11));
			e.setAutoScored(rs.getBoolean(12));
			e.setComments(rs.getString(13));
			e.setSize(rs.getInt(14));
			e.setScore(rs.getInt(15));
			e.setStage(rs.getInt(16));
			e.setAcademy(rs.getBoolean(17));

			// If we're joining with pilots, get the pilot name
			if (hasName) {
				e.setFirstName(rs.getString(18));
				e.setLastName(rs.getString(19));
			}

			// Add to results
			results.add(e);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to parse the check ride result set.
	 */
	private List<CheckRide> executeCheckride() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();
		boolean hasAcademy = (rs.getMetaData().getColumnCount() > 15);

		// Iterate through the results
		List<CheckRide> results = new ArrayList<CheckRide>();
		while (rs.next()) {
			CheckRide cr = new CheckRide(rs.getString(2));
			cr.setID(rs.getInt(1));
			cr.setPilotID(rs.getInt(3));
			cr.setFlightID(rs.getInt(4));
			cr.setStatus(rs.getInt(5));
			cr.setDate(rs.getTimestamp(6));
			cr.setSubmittedOn(rs.getTimestamp(7));
			cr.setScoredOn(rs.getTimestamp(8));
			cr.setScorerID(rs.getInt(9));
			cr.setPassFail(rs.getBoolean(10));
			cr.setComments(rs.getString(11));
			cr.setAircraftType(rs.getString(12));
			cr.setEquipmentType(rs.getString(13));
			cr.setAcademy(rs.getBoolean(14));
			cr.setStage(rs.getInt(15));
			if (hasAcademy)
				cr.setCourseID(rs.getInt(16));

			// Add to results
			results.add(cr);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 8.0
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
			prepareStatementWithoutLimits("SELECT E.*, COUNT(DISTINCT EQ.QUESTION_NO), SUM(EQ.CORRECT), EP.STAGE, EP.ACADEMY, EP.AIRLINE FROM exams.EXAMS E, exams.EXAMQUESTIONS EQ, "
				+ "exams.EXAMINFO EP WHERE (E.ID=?) AND (E.NAME=EP.NAME) AND (E.ID=EQ.EXAM_ID) GROUP BY E.ID LIMIT 1");
			_ps.setInt(1, id);

			// Execute the query, return null if nothing
			List<Examination> results = execute();
			if (results.isEmpty())
				return null;

			// Load the questions for this examination
			Examination e = results.get(0);
			prepareStatementWithoutLimits("SELECT EQ.QUESTION_ID, EQ.QUESTION_NO, EQA.QUESTION, EQA.CORRECT_ANSWER, EQA.ANSWER, EQ.CORRECT, COUNT(MQ.SEQ), QI.TYPE, QI.SIZE, QI.X, QI.Y, RQ.AIRPORT_D, RQ.AIRPORT_A "
				+ "FROM exams.EXAMQANSWERS EQA, exams.EXAMQUESTIONS EQ LEFT JOIN exams.EXAMQUESTIONSM MQ ON (EQ.EXAM_ID=MQ.EXAM_ID) AND (EQ.QUESTION_ID=MQ.QUESTION_ID) LEFT JOIN exams.QUESTIONIMGS "
				+ "QI ON (EQ.QUESTION_ID=QI.ID) LEFT JOIN exams.EXAMQUESTIONSRP RQ ON (EQ.EXAM_ID=RQ.EXAM_ID) AND (EQ.QUESTION_ID=RQ.QUESTION_ID) WHERE (EQ.EXAM_ID=EQA.EXAM_ID) AND (EQ.EXAM_ID=?) "
				+ "AND (EQ.QUESTION_NO=EQA.QUESTION_NO) GROUP BY EQ.QUESTION_ID, EQ.QUESTION_NO ORDER BY EQ.QUESTION_NO");
			_ps.setInt(1, id);

			// Execute the query
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					boolean isMC = (rs.getInt(7) > 0);
					boolean isRP = (rs.getString(13) != null);

					// Create the question
					Question q = null;
					if (isRP) {
						RoutePlotQuestion rpq = new RoutePlotQuestion(rs.getString(3));
						rpq.setAirportD(SystemData.getAirport(rs.getString(12)));
						rpq.setAirportA(SystemData.getAirport(rs.getString(13)));
						q = rpq;
					} else if (isMC)
						q = new MultiChoiceQuestion(rs.getString(3));
					else
						q = new Question(rs.getString(3));
				
					// Populate the fields
					q.setID(rs.getInt(1));
					q.setNumber(rs.getInt(2));
					q.setCorrectAnswer(rs.getString(4));
					q.setAnswer(rs.getString(5));
					q.setCorrect(rs.getBoolean(6));
					if (rs.getInt(9) > 0) {
						q.setType(rs.getInt(8));
						q.setSize(rs.getInt(9));
						q.setWidth(rs.getInt(10));
						q.setHeight(rs.getInt(11));
					}

					e.addQuestion(q);
				}
			}

			_ps.close();

			// Load multiple choice questions
			if (e.hasMultipleChoice()) {
				Map<Integer, Question> qMap = CollectionUtils.createMap(e.getQuestions(), Question::getID);
				prepareStatementWithoutLimits("SELECT QUESTION_ID, SEQ, ANSWER FROM exams.EXAMQUESTIONSM WHERE (EXAM_ID=?) ORDER BY QUESTION_ID, SEQ");
				_ps.setInt(1, e.getID());

				// Execute the query
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next()) {
						Question q = qMap.get(Integer.valueOf(rs.getInt(1)));
						if (q != null) {
							MultiChoiceQuestion mq = (MultiChoiceQuestion) q;
							mq.addChoice(rs.getString(3));
						}
					}
				}

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
			prepareStatementWithoutLimits("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF "
				+ "ON (CR.ID=CF.ID) LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) AND (CR.ID=?) LIMIT 1");
			_ps.setInt(1, id);
			List<CheckRide> results = executeCheckride();
			return results.isEmpty() ? null : results.get(0);
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
			prepareStatementWithoutLimits("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF "
				+ "ON (CR.ID=CF.ID) LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) AND (CF.ACARS_ID=?) LIMIT 1");
			_ps.setInt(1, acarsID);
			List<CheckRide> results = executeCheckride();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Check Rides for a particular Flight Academy Course.
	 * @param courseID the Course database ID
	 * @return a List of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRide> getAcademyCheckRides(int courseID) throws DAOException {
		return getAcademyCheckRides(courseID, null);
	}
	
	/**
	 * Loads all Check Rides for a particular Flight Academy Course.
	 * @param courseID the Course database ID
	 * @param status the CheckRide status, or null for all
	 * @return a List of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRide> getAcademyCheckRides(int courseID, TestStatus status) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF "
				+ "ON (CR.ID=CF.ID) LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) AND (CRR.COURSE=?)");
		if (status != null)
			sqlBuf.append(" AND (CR.STATUS=?)");
		sqlBuf.append(" ORDER BY CR.CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, courseID);
			if (status != null)
				_ps.setInt(2, status.ordinal());
			
			return executeCheckride();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads a pending Pilot Check Ride for a particular equipment type.
	 * @param pilotID the Pilot Database ID
	 * @param eqType the equipment type used
	 * @param status the CheckRide status
	 * @return a CheckRide, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRide getCheckRide(int pilotID, String eqType, TestStatus status) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF "
				+ "ON (CR.ID=CF.ID) LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) AND (CR.PILOT_ID=?) AND (CR.ACTYPE=?) "
				+ "AND (CR.STATUS=?) LIMIT 1");
			_ps.setInt(1, pilotID);
			_ps.setString(2, eqType);
			_ps.setInt(3, status.ordinal());
			List<CheckRide> results = executeCheckride();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Check Rides for a particular Pilot.
	 * @param pilotID the pilot Database ID
	 * @return a List of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRide> getCheckRides(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF ON (CR.ID=CF.ID) "
				+ "LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.PILOT_ID=?) AND (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) ORDER BY CR.CREATED");
			_ps.setInt(1, pilotID);
			return executeCheckride();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all unsubmitted Check Rides older than a certain date.
	 * @param days the number of days
	 * @param rt a RideType
	 * @return a List of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CheckRide> getPendingRides(int days, RideType rt) throws DAOException {
		try {
			prepareStatement("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF ON (CR.ID=CF.ID) "
				+ "LEFT JOIN exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.TYPE=?) AND (CR.STATUS=?) AND (CR.CREATED < DATE_SUB(NOW(), INTERVAL ? DAY)) AND (CR.EQTYPE=EQ.EQTYPE) "
				+ "AND (CR.OWNER=EQ.OWNER) ORDER BY CR.CREATED");
			_ps.setInt(1, rt.ordinal());
			_ps.setInt(2, TestStatus.NEW.ordinal());
			_ps.setInt(3, days);
			return executeCheckride();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all submitted Check Rides for the current airline or the Flight Academy.
	 * @param isAcademy TRUE if listing Flight Academy Check Rides, otherwise FALSE
	 * @return a Collection of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CheckRide> getCheckRideQueue(boolean isAcademy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF "
			+ "ON (CR.ID=CF.ID) LEFT JOIN exams.COURSERIDES CRR ON (CRR.CHECKRIDE=CR.ID) WHERE (CR.EQTYPE=EQ.EQTYPE) AND (CR.OWNER=EQ.OWNER) AND (CR.STATUS=?) AND ");
		if (isAcademy)
			sqlBuf.append("(CR.ACADEMY=?)");
		else
			sqlBuf.append("(EQ.OWNER=?)");
		sqlBuf.append(" ORDER BY CR.CREATED");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, TestStatus.SUBMITTED.ordinal());
			if (isAcademy)
				_ps.setBoolean(2, isAcademy);
			else
				_ps.setString(2, SystemData.get("airline.code"));
			
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
			prepareStatement("SELECT E.*, COUNT(DISTINCT EQ.QUESTION_NO), SUM(EQ.CORRECT), EP.STAGE, EP.ACADEMY, EP.AIRLINE FROM exams.EXAMS E, exams.EXAMQUESTIONS EQ, exams.EXAMINFO EP "
				+ "WHERE (E.PILOT_ID=?) AND (EP.NAME=E.NAME) AND (E.ID=EQ.EXAM_ID) GROUP BY E.ID");
			_ps.setInt(1, id);
			List<Test> results = new ArrayList<Test>(execute());

			// Load Check Rides
			prepareStatement("SELECT CR.*, CF.ACARS_ID, EQ.STAGE, CRR.COURSE FROM (exams.CHECKRIDES CR, common.EQPROGRAMS EQ) LEFT JOIN exams.CHECKRIDE_FLIGHTS CF ON (CR.ID=CF.ID) LEFT JOIN "
				+ "exams.COURSERIDES CRR ON (CR.ID=CRR.CHECKRIDE) WHERE (CR.PILOT_ID=?) AND (CR.EQTYPE=EQ.EQTYPE) AND (EQ.OWNER=CR.OWNER)");
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
		StringBuilder sqlBuf = new StringBuilder("SELECT STRAIGHT_JOIN E.*, COUNT(DISTINCT EQ.QUESTION_NO), SUM(EQ.CORRECT), EP.STAGE, EP.ACADEMY, EP.AIRLINE, P.FIRSTNAME, P.LASTNAME "
			+ "FROM exams.EXAMS E, exams.EXAMQUESTIONS EQ, PILOTS P, exams.EXAMINFO EP WHERE (P.ID=E.PILOT_ID) AND (E.NAME=EP.NAME) AND (E.AUTOSCORE=?) AND (E.ID=EQ.EXAM_ID) AND (EP.AIRLINE=?) ");
		if (examName != null)
			sqlBuf.append("AND (E.NAME=?) ");
		
		sqlBuf.append("GROUP BY E.ID DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			_ps.setString(2, SystemData.get("airline.code"));
			if (examName != null)
				_ps.setString(3, examName);
			
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
			prepareStatementWithoutLimits("SELECT DISTINCT E.NAME FROM exams.EXAMS E, exams. EXAMINFO EP WHERE (E.NAME=EP.NAME) AND (E.AUTOSCORE=?) AND (EP.AIRLINE=?)");
			_ps.setBoolean(1, true);
			_ps.setString(2, SystemData.get("airline.code"));
			Collection<String> results = new TreeSet<String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
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
		StringBuilder sqlBuf = new StringBuilder("SELECT E.*, COUNT(DISTINCT EQ.QUESTION_NO), SUM(EQ.CORRECT), EP.STAGE, EP.ACADEMY, EP.AIRLINE FROM exams.EXAMS E, exams.EXAMQUESTIONS EQ, exams.EXAMINFO EP "
			+ "WHERE (E.NAME=?) AND (EP.NAME=E.NAME) AND (E.ID=EQ.EXAM_ID) AND (E.ID IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) GROUP BY E.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME);
			return CollectionUtils.createMap(execute(), Examination::getAuthorID);
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
			prepareStatement("SELECT E.*, COUNT(DISTINCT EQ.QUESTION_NO), SUM(EQ.CORRECT), EP.STAGE, EP.ACADEMY, EP.AIRLINE FROM exams.EXAMS E, exams.EXAMQUESTIONS EQ, "
				+ "exams.EXAMINFO EP WHERE (E.NAME=EP.NAME) AND ((E.STATUS=?) OR ((E.STATUS=?) AND (E.EXPIRY_TIME < NOW()))) AND (E.ID=EQ.EXAM_ID) AND (EP.AIRLINE=?) GROUP BY "
				+ "E.ID ORDER BY E.CREATED_ON");
			_ps.setInt(1, TestStatus.SUBMITTED.ordinal());
			_ps.setInt(2, TestStatus.NEW.ordinal());
			_ps.setString(3, SystemData.get("airline.code"));
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
			prepareStatementWithoutLimits("SELECT ID FROM exams.EXAMS WHERE (PILOT_ID=?) AND ((STATUS=?) OR (STATUS=?)) LIMIT 1");
			_ps.setInt(1, id);
			_ps.setInt(2, TestStatus.NEW.ordinal());
			_ps.setInt(3, TestStatus.SUBMITTED.ordinal());

			// Execute the query
			int testID = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					testID = rs.getInt(1);
			}

			_ps.close();
			return testID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse the examination result set.
	 */
	private List<Examination> execute() throws SQLException {
		List<Examination> results = new ArrayList<Examination>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasName = (rs.getMetaData().getColumnCount() > 19);
			while (rs.next()) {
				Examination e = new Examination(rs.getString(2));
				e.setID(rs.getInt(1));
				e.setAuthorID(rs.getInt(3));
				e.setStatus(TestStatus.values()[rs.getInt(4)]);
				e.setDate(toInstant(rs.getTimestamp(5)));
				e.setExpiryDate(toInstant(rs.getTimestamp(6)));
				e.setSubmittedOn(toInstant(rs.getTimestamp(7)));
				e.setScoredOn(toInstant(rs.getTimestamp(8)));
				e.setScorerID(rs.getInt(9));
				e.setPassFail(rs.getBoolean(10));
				e.setEmpty(rs.getBoolean(11));
				e.setAutoScored(rs.getBoolean(12));
				e.setComments(rs.getString(13));
				e.setSize(rs.getInt(14));
				e.setScore(rs.getInt(15));
				e.setStage(rs.getInt(16));
				e.setAcademy(rs.getBoolean(17));
				e.setOwner(SystemData.getApp(rs.getString(18)));
				if (hasName) {
					e.setFirstName(rs.getString(19));
					e.setLastName(rs.getString(20));
				}

				results.add(e);
			}
		}

		_ps.close();
		return results;
	}

	/*
	 * Helper method to parse the check ride result set.
	 */
	private List<CheckRide> executeCheckride() throws SQLException {
		List<CheckRide> results = new ArrayList<CheckRide>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasAcademy = (rs.getMetaData().getColumnCount() > 18);
			while (rs.next()) {
				CheckRide cr = new CheckRide(rs.getString(2));
				cr.setID(rs.getInt(1));
				cr.setAuthorID(rs.getInt(3));
				cr.setStatus(TestStatus.values()[rs.getInt(4)]);
				cr.setDate(toInstant(rs.getTimestamp(5)));
				cr.setSubmittedOn(toInstant(rs.getTimestamp(6)));
				cr.setScoredOn(toInstant(rs.getTimestamp(7)));
				cr.setScorerID(rs.getInt(8));
				cr.setPassFail(rs.getBoolean(9));
				cr.setType(RideType.values()[rs.getInt(10)]);
				cr.setExpirationDate(toInstant(rs.getTimestamp(11)));
				cr.setComments(rs.getString(12));
				cr.setAircraftType(rs.getString(13));
				cr.setEquipmentType(rs.getString(14));
				cr.setAcademy(rs.getBoolean(15));
				cr.setOwner(SystemData.getApp(rs.getString(16)));
				cr.setFlightID(rs.getInt(17));
				cr.setStage(rs.getInt(18));
				if (hasAcademy)
					cr.setCourseID(rs.getInt(19));

				results.add(cr);
			}
		}

		_ps.close();
		return results;
	}
}
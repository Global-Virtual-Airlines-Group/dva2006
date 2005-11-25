// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;
import org.deltava.comparators.TestComparator;

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
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE FROM EXAMS E, "
					+ "EXAMQUESTIONS Q, EXAMINFO EP WHERE (E.ID=?) AND (E.NAME=EP.NAME) AND (E.ID=Q.EXAM_ID) "
					+ "GROUP BY E.ID");
			_ps.setInt(1, id);
			
			// Execute the query, return null if nothing
			List results = execute();
			if (results.isEmpty())
			   return null;
			
			// Get the examination
			Examination e = (Examination) results.get(0);
						
			// Load the questions for this examination
			prepareStatementWithoutLimits("SELECT * FROM EXAMQUESTIONS WHERE (EXAM_ID=?) ORDER BY QUESTION_NO");
			_ps.setInt(1, id);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Question q = new Question(rs.getString(4));
				q.setID(rs.getInt(2));
				q.setCorrectAnswer(rs.getString(5));
				q.setAnswer(rs.getString(6));
				q.setCorrect(rs.getBoolean(7));
				
				// Add question to the examination
				e.addQuestion(q);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
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
			prepareStatement("SELECT * FROM CHECKRIDES WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query
			List results = executeCheckride();
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
			prepareStatement("SELECT * FROM CHECKRIDES WHERE (ACARS_ID=?)");
			_ps.setInt(1, acarsID);

			// Execute the query
			List results = executeCheckride();
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
	 * @return a CheckRide, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CheckRide getCheckRide(String dbName, int pilotID, String eqType) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".CHECKRIDES WHERE (PILOT_ID=?) AND (NAME=?) AND (STATUS=?)");
	   
	   try {
	      setQueryMax(1);
	      prepareStatement(sqlBuf.toString());
	      _ps.setInt(1, pilotID);
	      _ps.setString(2, eqType + " Check Ride");
	      _ps.setInt(3, Test.NEW);
	      
			// Execute the query
			List results = executeCheckride();
			return results.isEmpty() ? null : (CheckRide) results.get(0);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Loads all Check Rides for a particular Pilot
	 * @param pilotID the pilot Database ID
	 * @return a Collection of CheckRide beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection getCheckRides(int pilotID) throws DAOException {
	   try {
	      prepareStatement("SELECT * FROM CHECKRIDES WHERE (PILOT_ID=?) ORDER BY CREATED");
	      _ps.setInt(1, pilotID);
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
	@SuppressWarnings("unchecked")
	public List<Test> getExams(int id) throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE " +
			      "FROM EXAMS E, EXAMQUESTIONS Q, EXAMINFO EP WHERE (E.PILOT_ID=?) AND (EP.NAME=E.NAME) " +
			      "AND (E.ID=Q.EXAM_ID) GROUP BY E.ID");
			_ps.setInt(1, id);
			
			// Execute the query
			List<Test> results = new ArrayList<Test>();
			results.addAll(execute());
			
			// Load videos
			prepareStatement("SELECT * FROM CHECKRIDES WHERE (PILOT_ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query
			results.addAll(executeCheckride());
			
			// Sort the results to merge them in by date
			TestComparator tc = new TestComparator(TestComparator.DATE);
			Collections.sort(results, tc);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all submitted Examinations and Check Rides.
	 * @return a List of Tests
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getSubmitted() throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(DISTINCT Q.QUESTION_NO), SUM(Q.CORRECT), EP.STAGE, " +
			      "P.FIRSTNAME, P.LASTNAME FROM EXAMS E, EXAMQUESTIONS Q, PILOTS P, EXAMINFO EP WHERE " +
			      "(P.ID=E.PILOT_ID) AND (E.NAME=EP.NAME) AND (E.STATUS=?) AND (E.ID=Q.EXAM_ID) " +
			      "GROUP BY E.ID ORDER BY E.CREATED_ON");
			_ps.setInt(1, Test.SUBMITTED);
			
			// Execute the query
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
	   boolean hasName = (rs.getMetaData().getColumnCount() > 13);
	   
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
			e.setSize(rs.getInt(11));
			e.setScore(rs.getInt(12));
			e.setStage(rs.getInt(13));
			
			// If we're joining with pilots, get the pilot name
			if (hasName) {
			   e.setFirstName(rs.getString(14));
			   e.setLastName(rs.getString(15));
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
	   
	   // Iterate through the results
	   List<CheckRide> results = new ArrayList<CheckRide>();
	   while (rs.next()) {
			CheckRide cr = new CheckRide(rs.getString(2));
			cr.setID(rs.getInt(1));
			cr.setPilotID(rs.getInt(3));
			cr.setFlightID(rs.getInt(4));
			cr.setStatus(rs.getInt(5));
			cr.setStage(rs.getInt(6));
			cr.setDate(rs.getTimestamp(7));
			cr.setSubmittedOn(rs.getTimestamp(8));
			cr.setScoredOn(rs.getTimestamp(9));
			cr.setScorerID(rs.getInt(10));
			cr.setPassFail(rs.getBoolean(11));
			cr.setComments(rs.getString(12));

			// Add to results
			results.add(cr);
	   }
	   
	   // Clean up and return
	   rs.close();
	   _ps.close();
	   return results;
	}
}
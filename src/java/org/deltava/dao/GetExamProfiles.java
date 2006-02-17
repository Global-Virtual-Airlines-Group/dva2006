// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

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
	 * @param isAcademy TRUE if Flight Academy exams should be returned, otherwise FALSE for
	 * the Testing Center
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
			prepareStatement("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM QUESTIONINFO Q "
					+ "LEFT JOIN EXAMQUESTIONS EQ ON (Q.ID=EQ.QUESTION_ID) WHERE (Q.ID=?) GROUP BY Q.ID");
			_ps.setInt(1, id);
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
			   rs.close();
			   _ps.close();
			   return null;
			}
			
			// Populate the Question Profile
			QuestionProfile qp = new QuestionProfile(rs.getString(2));
			qp.setID(rs.getInt(1));
			qp.setCorrectAnswer(rs.getString(3));
			qp.setActive(rs.getBoolean(4));
			qp.setTotalAnswers(rs.getInt(5));
			qp.setCorrectAnswers(rs.getInt(6));
			
			// Clean up
			rs.close();
			_ps.close();
			
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
	 * @param isRandom randomly order questions
	 * @return a List of QuestionProfiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<QuestionProfile> getQuestionPool(String examName, boolean isRandom) throws DAOException {
	   
	   // Check if we're displaying all questions
	   boolean showAll = "ALL".equals(examName);
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM "
	         + "QUESTIONINFO Q LEFT JOIN EXAMQUESTIONS EQ ON (Q.ID=EQ.QUESTION_ID) LEFT JOIN QE_INFO QE "
			   + "ON (Q.ID=QE.QUESTION_ID) WHERE (Q.ACTIVE=?)");
	   
	   if (!showAll)
	      sqlBuf.append(" AND (QE.EXAM_NAME=?)");
	   
	   sqlBuf.append(" GROUP BY Q.ID");
	   if (isRandom)
		   sqlBuf.append(" ORDER BY RAND()");
	   
		try {
		   prepareStatement(sqlBuf.toString());
		   _ps.setBoolean(1, true);
		   if (!showAll)
		      _ps.setString(2, examName);
			
			// Execute the Query
			List<QuestionProfile> results = new ArrayList<QuestionProfile>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				QuestionProfile qp = new QuestionProfile(rs.getString(2));
				qp.setID(rs.getInt(1));
				qp.setCorrectAnswer(rs.getString(3));
				qp.setActive(rs.getBoolean(4));
				qp.setTotalAnswers(rs.getInt(5));
				qp.setCorrectAnswers(rs.getInt(6));
				
				// Add to results
				results.add(qp);
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
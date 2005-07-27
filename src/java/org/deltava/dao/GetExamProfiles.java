// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
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
	
	public ExamProfile getExamProfile(String examName) throws DAOException {
		try {
			prepareStatement("SELECT * FROM EXAMINFO WHERE (NAME=?)");
			_ps.setString(1, examName);
			
			// Execute the query - return null if not found
			ResultSet rs = _ps.executeQuery();
			if (!rs.next())
				return null;
			
			// Populate the examination profile
			ExamProfile ep = new ExamProfile(rs.getString(1));
			ep.setStage(rs.getInt(2));
			ep.setMinStage(rs.getInt(3));
			ep.setEquipmentType(rs.getString(4));
			ep.setSize(rs.getInt(5));
			ep.setPassScore(rs.getInt(6));
			ep.setTime(rs.getInt(7));
			ep.setActive(rs.getBoolean(8));
			ep.setNeedsCheckRide(rs.getBoolean(9));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return ep;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	public List getExamProfiles() throws DAOException {
		try {
			prepareStatement("SELECT * FROM EXAMINFO ORDER BY STAGE, NAME");
			
			// Execute the query
			List results = new ArrayList();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExamProfile ep = new ExamProfile(rs.getString(1));
				ep.setStage(rs.getInt(2));
				ep.setMinStage(rs.getInt(3));
				ep.setEquipmentType(rs.getString(4));
				ep.setSize(rs.getInt(5));
				ep.setPassScore(rs.getInt(6));
				ep.setTime(rs.getInt(7));
				ep.setActive(rs.getBoolean(8));
				ep.setNeedsCheckRide(rs.getBoolean(9));

				// Add to results
				results.add(ep);
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
	 * Loads a Question Profile.
	 * @param id the Question ID
	 * @return the Question profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public QuestionProfile getQuestionProfile(int id) throws DAOException {
		try {
			prepareStatement("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM QUESTIONINFO Q, " +
					"EXAMQUESTIONS EQ WHERE (Q.ID=EQ.QUESTION_ID) AND (Q.ID=?) GROUP BY Q.ID");
			_ps.setInt(1, id);
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (!rs.next())
				return null;
			
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
	 * @return a List of QuestionProfiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getQuestionPool(String examName) throws DAOException {
	   
	   // Check if we're displaying all questions
	   boolean showAll = "ALL".equals(examName);
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT Q.*, COUNT(EQ.CORRECT), SUM(EQ.CORRECT) FROM " +
	         "QUESTIONINFO Q, EXAMQUESTIONS EQ, QE_INFO QE WHERE (Q.ID=EQ.QUESTION_ID) AND " +
	         "(Q.ID=QE.QUESTION_ID) ");
	   
	   if (!showAll)
	      sqlBuf.append("AND (QE.EXAM_NAME=?) ");
	   
	   sqlBuf.append("GROUP BY Q.ID ORDER BY Q.ID");
	   
		try {
		   prepareStatement(sqlBuf.toString());
		   if (!showAll)
		      _ps.setString(1, examName);
			
			// Execute the Query
			List results = new ArrayList();
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
}
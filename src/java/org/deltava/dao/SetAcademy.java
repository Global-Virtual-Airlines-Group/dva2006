// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to write Flight Academy data to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetAcademy extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAcademy(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Certification to the database.
	 * @param c the Certification bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Certification c) throws DAOException {
		try {
			startTransaction();
			
			// Write the certification entry
			prepareStatementWithoutLimits("INSERT INTO CERTS (NAME, STAGE, PREREQ, ACTIVE) VALUES (?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			_ps.setInt(2, c.getStage());
			_ps.setInt(3, c.getReqs());
			_ps.setBoolean(4, c.getActive());
			executeUpdate(1);
			
			// Write the exams
			writeExams(c.getName(), c.getExamNames());
			
			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing Flight Academy Certification profile.
	 * @param c
	 * @param name the Certification name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Certification c, String name) throws DAOException {
		try {
			startTransaction();
			
			// Write the profile
			prepareStatement("UPDATE CERTS SET NAME=?, STAGE=?, PREREQ=?, ACTIVE=? WHERE (NAME=?)");
			_ps.setString(1, c.getName());
			_ps.setInt(2, c.getStage());
			_ps.setInt(3, c.getReqs());
			_ps.setBoolean(4, c.getActive());
			_ps.setString(5, name);
			executeUpdate(1);
			
			// Clear the exams
			prepareStatementWithoutLimits("DELETE FROM CERTEXAMS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the requirements
			prepareStatementWithoutLimits("DELETE FROM CERTREQS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Write the exams
			writeExams(c.getName(), c.getExamNames());
			
			// Write the requirements
			prepareStatementWithoutLimits("INSERT INTO CERTREQS (CERTNAME, SEQ, REQENTRY) VALUES (?, ?, ?)");
			_ps.setString(1, c.getName());
			for (Iterator<CertificationRequirement> i = c.getRequirements().iterator(); i.hasNext(); ) {
				CertificationRequirement req = i.next();
				_ps.setInt(2, req.getID());
				_ps.setString(3, req.getText());
				_ps.addBatch();
			}
			
			// Execute the batch transaction
			_ps.executeBatch();
			_ps.close();

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight Academy Course entry to the database. 
	 * @param c the Course bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Course c) throws DAOException {
		try {
			startTransaction();
			
			// Prepare the statement
			if (c.getID() == 0) {
				prepareStatement("INSERT INTO COURSES (CERTNAME, PILOT_ID, STATUS, STARTDATE) VALUES (?, ?, ?, ?)");
			} else {
				prepareStatement("UPDATE COURSES SET CERTNAME=?, PILOT_ID=?, STATUS=?, STARTDATE=?, ENDDATE=? "
						+ "WHERE (ID=?)");
				_ps.setTimestamp(5, createTimestamp(c.getEndDate()));
				_ps.setInt(6, c.getID());
			}
			
			// Set parameters and execute
			_ps.setString(1, c.getName());
			_ps.setInt(2, c.getPilotID());
			_ps.setInt(3, c.getStatus());
			_ps.setTimestamp(4, createTimestamp(c.getStartDate()));
			executeUpdate(1);
			
			// Get the new database ID or clear course progress
			if (c.getID() == 0) {
				c.setID(getNewID());
			} else {
				prepareStatementWithoutLimits("DELETE FROM COURSEPROGRESS WHERE (ID=?)");
				_ps.setInt(1, c.getID());
				executeUpdate(0);
			}
			
			// Write course progress
			for (Iterator<CourseProgress> i = c.getProgress().iterator(); i.hasNext(); ) {
				CourseProgress cp = i.next();
				if (cp.getCourseID() == 0)
					cp.setCourseID(c.getID());
				
				updateProgress(cp);
			}
			
			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Course comment to the database.
	 * @param cc the CourseComment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void comment(CourseComment cc) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO COURSECHAT (COURSE_ID, PILOT_ID, CREATED, COMMENTS) "
					+ "VALUES (?, ?, ?, ?)");
			_ps.setInt(1, cc.getID());
			_ps.setInt(2, cc.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(cc.getCreatedOn()));
			_ps.setString(4, cc.getText());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a Course requirement as complete.
	 * @param courseID the Course database ID
	 * @param seq the requirement ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void complete(int courseID, int seq) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE COURSEPROGRESS SET COMPLETE=?, COMPLETED=NOW() WHERE "
					+ "(ID=?) AND (SEQ=?) AND (COMPLETE=?)");
			_ps.setBoolean(1, true);
			_ps.setInt(2, courseID);
			_ps.setInt(3, seq);
			_ps.setBoolean(4, false);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates a Flight Academy Course progress entry.
	 * @param cp the CourseProgress bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateProgress(CourseProgress cp) throws DAOException {
		try {
			prepareStatement("REPLACE INTO COURSEPROGRESS (ID, SEQ, REQENTRY, COMPLETE, COMPLETED) "
					+ "VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, cp.getCourseID());
			_ps.setInt(2, cp.getID());
			_ps.setString(3, cp.getText());
			_ps.setBoolean(4, cp.getComplete());
			_ps.setTimestamp(5, createTimestamp(cp.getCompletedOn()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates a Flight Academy Course's status.
	 * @param courseID the database ID of the course
	 * @param status the status code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setStatus(int courseID, int status) throws DAOException {
		try {
			prepareStatement("UPDATE COURSES SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, status);
			_ps.setInt(2, courseID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight Academy Course from the database.
	 * @param courseID the database ID of the course
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int courseID) throws DAOException {
		try {
			prepareStatement("DELETE FROM COURSES WHERE (ID=?)");
			_ps.setInt(1, courseID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight Academy Certification from the database.
	 * @param certName the certification name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String certName) throws DAOException {
		try {
			prepareStatement("DELETE FROM CERTS WHERE (NAME=?)");
			_ps.setString(1, certName);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an Instruction Calendar entry.
	 * @param s the InstructionSession bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(InstructionSession s) throws DAOException {
		try {
			prepareStatement("REPLACE INTO INSCALENDAR (COURSE, INSTRUCTOR_ID, STARTTIME, ENDTIME, "
					+ "STATUS, NOSHOW, REMARKS, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, s.getCourseID());
			_ps.setInt(2, s.getInstructorID());
			_ps.setTimestamp(3, createTimestamp(s.getStartTime()));
			_ps.setTimestamp(4, createTimestamp(s.getEndTime()));
			_ps.setInt(5, s.getStatus());
			_ps.setBoolean(6, s.getNoShow());
			_ps.setString(7, s.getRemarks());
			_ps.setInt(8, s.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to write exam names.
	 */
	private void writeExams(String certName, Collection<String> exams) throws SQLException {

		// Prepare the statement
		prepareStatementWithoutLimits("INSERT INTO CERTEXAMS (CERTNAME, EXAMNAME) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (Iterator<String> i = exams.iterator(); i.hasNext(); ) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}
		
		// Execute the batch transaction
		_ps.executeBatch();
		_ps.close();
		_ps = null;
	}
}
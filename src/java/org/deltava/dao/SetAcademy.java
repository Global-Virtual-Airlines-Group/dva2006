// Copyright 2006, 2007, 2008, 2010, 2012, 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to write Flight Academy Course data to the database.
 * @author Luke
 * @version 7.2
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
	 * Writes a Flight Academy Course entry to the database. 
	 * @param c the Course bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Course c) throws DAOException {
		try {
			startTransaction();
			
			// Prepare the statement
			if (c.getID() == 0) {
				prepareStatement("INSERT INTO exams.COURSES (CERTNAME, PILOT_ID, INSTRUCTOR_ID, STATUS, STARTDATE, CHECKRIDES) VALUES (?, ?, ?, ?, ?, ?)");
				_ps.setInt(6, c.getRideCount());
			} else {
				prepareStatement("UPDATE exams.COURSES SET CERTNAME=?, PILOT_ID=?, INSTRUCTOR_ID=?, STATUS=?, STARTDATE=?, ENDDATE=? WHERE (ID=?)");
				_ps.setTimestamp(6, createTimestamp(c.getEndDate()));
				_ps.setInt(7, c.getID());
			}
			
			// Set parameters and execute
			_ps.setString(1, c.getName());
			_ps.setInt(2, c.getPilotID());
			_ps.setInt(3, c.getInstructorID());
			_ps.setInt(4, c.getStatus().ordinal());
			_ps.setTimestamp(5, createTimestamp(c.getStartDate()));
			executeUpdate(1);
			
			// Get the new database ID or clear course progress
			if (c.getID() == 0)
				c.setID(getNewID());
			else if (!c.getProgress().isEmpty()) {
				prepareStatementWithoutLimits("DELETE FROM exams.COURSEPROGRESS WHERE (ID=?)");
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
			prepareStatementWithoutLimits("INSERT INTO exams.COURSECHAT (COURSE_ID, PILOT_ID, CREATED, COMMENTS) VALUES (?, ?, ?, ?)");
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
			prepareStatementWithoutLimits("UPDATE exams.COURSEPROGRESS SET COMPLETE=?, COMPLETED=NOW() WHERE (ID=?) AND (SEQ=?) AND (COMPLETE=?)");
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
			prepareStatement("REPLACE INTO exams.COURSEPROGRESS (ID, SEQ, AUTHOR, REQENTRY, EXAMNAME, COMPLETE, COMPLETED) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, cp.getCourseID());
			_ps.setInt(2, cp.getID());
			_ps.setInt(3, cp.getAuthorID());
			_ps.setString(4, cp.getText());
			_ps.setString(5, cp.getExamName());
			_ps.setBoolean(6, cp.getComplete());
			_ps.setTimestamp(7, createTimestamp(cp.getCompletedOn()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates a Flight Academy Course's status.
	 * @param courseID the database ID of the course
	 * @param s the Status
	 * @param sd the updated course start date
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setStatus(int courseID, Status s, java.time.Instant sd) throws DAOException {
		try {
			prepareStatement("UPDATE exams.COURSES SET STATUS=?, STARTDATE=?, ENDDATE=IF(STATUS=?, NOW(), NULL) WHERE (ID=?)");
			_ps.setInt(1, s.ordinal());
			_ps.setTimestamp(2, createTimestamp(sd));
			_ps.setInt(3, Status.COMPLETE.ordinal());
			_ps.setInt(4, courseID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Assigns a Flight Academy course to a different pilot, when transferring airlines.
	 * @param courseID the database ID of the course
	 * @param newPilotID the database ID of the new Pilot
	 * @throws DAOException if a JDBC error occurs
	 */
	public void reassign(int courseID, int newPilotID) throws DAOException {
		try {
			prepareStatement("UPDATE exams.COURSES SET PILOT_ID=? WHERE (ID=?)");
			_ps.setInt(1, newPilotID);
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
			prepareStatement("DELETE FROM exams.COURSES WHERE (ID=?)");
			_ps.setInt(1, courseID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes all Flight Academy certifications associated with a particular Video.
	 * @param video the Video bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeCertifications(TrainingVideo video) throws DAOException {
		try {
			startTransaction();
			
			// Clean out the certifications
			prepareStatementWithoutLimits("DELETE FROM exams.CERTVIDEOS WHERE (FILENAME=?)");
			_ps.setString(1, video.getFileName());
			executeUpdate(0);
			
			// Add the certifications
			prepareStatementWithoutLimits("INSERT INTO exams.CERTVIDEOS (CERT, FILENAME) VALUES (?, ?)");
			_ps.setString(2, video.getFileName());
			for (Iterator<String> i = video.getCertifications().iterator(); i.hasNext(); ) {
				_ps.setString(1, i.next());
				_ps.addBatch();
			}
			
			// Execute the batch transaction and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
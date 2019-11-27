// Copyright 2006, 2007, 2008, 2010, 2012, 2014, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to write Flight Academy Course data to the database.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO exams.COURSES (CERTNAME, PILOT_ID, INSTRUCTOR_ID, STATUS, STARTDATE, ENDDATE, CHECKRIDES) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPCATE KEY UPDATE "
				+ "CERTNAME=VALUES(CERTNAME), PILOT_ID=VALUES(PILOT_ID), INSTRUCTOR_ID=VALUES(INSTRUCTOR_ID), STATUS=VALUES(STATUS), STARTDATE=VALUES(STARTDATE), DNDDATE=VALUES(ENDDATE)")) {
				ps.setString(1, c.getName());
				ps.setInt(2, c.getPilotID());
				ps.setInt(3, c.getInstructorID());
				ps.setInt(4, c.getStatus().ordinal());
				ps.setTimestamp(5, createTimestamp(c.getStartDate()));
				ps.setTimestamp(6, createTimestamp(c.getEndDate()));
				ps.setInt(7, c.getRideCount());
				ps.setInt(7, c.getID());
				executeUpdate(ps, 1);
			}
			
			// Get the new database ID or clear course progress
			if (c.getID() == 0) c.setID(getNewID());
			if (!c.getProgress().isEmpty()) {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.COURSEPROGRESS WHERE (ID=?)")) {
					ps.setInt(1, c.getID());
					executeUpdate(ps, 0);
				}
			}
			
			// Write course progress
			for (CourseProgress cp : c.getProgress()) {
				if (cp.getCourseID() == 0) cp.setCourseID(c.getID());
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.COURSECHAT (COURSE_ID, PILOT_ID, CREATED, COMMENTS) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, cc.getID());
			ps.setInt(2, cc.getAuthorID());
			ps.setTimestamp(3, createTimestamp(cc.getCreatedOn()));
			ps.setString(4, cc.getText());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE exams.COURSEPROGRESS SET COMPLETE=?, COMPLETED=NOW() WHERE (ID=?) AND (SEQ=?) AND (COMPLETE=?)")) {
			ps.setBoolean(1, true);
			ps.setInt(2, courseID);
			ps.setInt(3, seq);
			ps.setBoolean(4, false);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("REPLACE INTO exams.COURSEPROGRESS (ID, SEQ, AUTHOR, REQENTRY, EXAMNAME, COMPLETE, COMPLETED) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, cp.getCourseID());
			ps.setInt(2, cp.getID());
			ps.setInt(3, cp.getAuthorID());
			ps.setString(4, cp.getText());
			ps.setString(5, cp.getExamName());
			ps.setBoolean(6, cp.getComplete());
			ps.setTimestamp(7, createTimestamp(cp.getCompletedOn()));
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("UPDATE exams.COURSES SET STATUS=?, STARTDATE=?, ENDDATE=IF(STATUS=?, NOW(), NULL) WHERE (ID=?)")) {
			ps.setInt(1, s.ordinal());
			ps.setTimestamp(2, createTimestamp(sd));
			ps.setInt(3, Status.COMPLETE.ordinal());
			ps.setInt(4, courseID);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("UPDATE exams.COURSES SET PILOT_ID=? WHERE (ID=?)")) {
			ps.setInt(1, newPilotID);
			ps.setInt(2, courseID);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.COURSES WHERE (ID=?)")) {
			ps.setInt(1, courseID);
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTVIDEOS WHERE (FILENAME=?)")) {
				ps.setString(1, video.getFileName());
				executeUpdate(ps, 0);
			}
			
			// Add the certifications
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTVIDEOS (CERT, FILENAME) VALUES (?, ?)")) {
				ps.setString(2, video.getFileName());
				for (String certName : video.getCertifications()) {
					ps.setString(1, certName);
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, video.getCertifications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
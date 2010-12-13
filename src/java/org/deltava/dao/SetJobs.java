// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to write Job Postings and Applications to the database.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class SetJobs extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetJobs(Connection c) {
		super(c);
	}

	/**
	 * Writes a job posting to the daatabase. This can handle INSERTs and UPDATEs.
	 * @param jp the JobPosting bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(JobPosting jp) throws DAOException {
		try {
			if (jp.getID() != 0) {
				prepareStatement("UPDATE JOBPOSTINGS SET CLOSES=?, STATUS=?, TITLE=?, MINLEGS=?, MINAGE=?, "
					+ "STAFF_ONLY=?, HIRE_MGR=?, SUMMARY=?, BODY=? WHERE (ID=?)");
				_ps.setInt(10, jp.getID());
			} else
				prepareStatement("INSERT INTO JOBPOSTINGS (CLOSES, STATUS, TITLE, MINLEGS, MINAGE, STAFF_ONLY, "
					+ "HIRE_MGR, SUMMARY, BODY, CREATED) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
			
			_ps.setTimestamp(1, createTimestamp(jp.getClosesOn()));
			_ps.setInt(2, jp.getStatus());
			_ps.setString(3, jp.getTitle());
			_ps.setInt(4, jp.getMinLegs());
			_ps.setInt(5, jp.getMinAge());
			_ps.setBoolean(6, jp.getStaffOnly());
			_ps.setInt(7, jp.getHireManagerID());
			_ps.setString(8, jp.getSummary());
			_ps.setString(9, jp.getDescription());
			executeUpdate(1);
			
			// Update ID
			if (jp.getID() == 0)
				jp.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Job Application to the database.
	 * @param a an Application bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Application a) throws DAOException {
		try {
			prepareStatement("REPLACE INTO JOBAPPS (ID, AUTHOR_ID, CREATED, STATUS, BODY) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, a.getID());
			_ps.setInt(2, a.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(a.getCreatedOn()));
			_ps.setInt(4, a.getStatus());
			_ps.setString(5, a.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an applicant profile to the database.
	 * @param p the Profile bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Profile p) throws DAOException {
		try {
			prepareStatement("REPLACE INTO JOBAPROFILES (ID, CREATED, AUTO_REUSE, BODY) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, p.getAuthorID());
			_ps.setTimestamp(2, createTimestamp(p.getCreatedOn()));
			_ps.setBoolean(3, p.getAutoReuse());
			_ps.setString(4, p.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Job Posting comment to the database.
	 * @param c the Comment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Comment c) throws DAOException {
		try {
			prepareStatement("INSERT INTO JOBCOMMENTS (ID, AUTHOR_ID, CREATED, BODY) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, c.getID());
			_ps.setInt(2, c.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(c.getCreatedOn()));
			_ps.setString(4, c.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a job posting from the database.
	 * @param id the database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteJob(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM JOBPOSTINGS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
	
	/**
	 * Deletes a job application from the database.
	 * @param id the application author database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteApplication(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM JOBAPPS WHERE (AUTHOR_ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an applicant profile from the database.
	 * @param id the profile database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteProfile(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM JOBAPROFILES WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
}
// Copyright 2010, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to write Job Postings and Applications to the database.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO JOBPOSTINGS (CLOSES, STATUS, TITLE, MINLEGS, MINAGE, STAFF_ONLY, HIRE_MGR, SUMMARY, BODY, CREATED, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?) "
				+ "ON DUPLICATE KEY UPDATE CLOSES=VALUES(CLOSES), STATUS=VALUES(STATUS), TITLE=VALUES(TITLE), MINLEGS=VALUES(MINLEGS), MINAGE=VALUES(MINAGE), STAFF_ONLY=VALUES(STAFF_ONLY), "
				+ "HIRE_MGR=VALUES(HIRE_MGR), SUMMARY=VALUES(SUMMARY), BODy=VALUES(BODY)")) {
				ps.setTimestamp(1, createTimestamp(jp.getClosesOn()));
				ps.setInt(2, jp.getStatus());
				ps.setString(3, jp.getTitle());
				ps.setInt(4, jp.getMinLegs());
				ps.setInt(5, jp.getMinAge());
				ps.setBoolean(6, jp.getStaffOnly());
				ps.setInt(7, jp.getHireManagerID());
				ps.setString(8, jp.getSummary());
				ps.setString(9, jp.getDescription());
				ps.setInt(10, jp.getID());
				executeUpdate(ps, 1);
			}
			
			if (jp.getID() == 0) jp.setID(getNewID());
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO JOBAPPS (ID, AUTHOR_ID, CREATED, STATUS, BODY) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, a.getID());
			ps.setInt(2, a.getAuthorID());
			ps.setTimestamp(3, createTimestamp(a.getCreatedOn()));
			ps.setInt(4, a.getStatus());
			ps.setString(5, a.getBody());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO JOBAPROFILES (ID, CREATED, AUTO_REUSE, BODY) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, p.getAuthorID());
			ps.setTimestamp(2, createTimestamp(p.getCreatedOn()));
			ps.setBoolean(3, p.getAutoReuse());
			ps.setString(4, p.getBody());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO JOBCOMMENTS (ID, AUTHOR_ID, CREATED, BODY) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, c.getID());
			ps.setInt(2, c.getAuthorID());
			ps.setTimestamp(3, createTimestamp(c.getCreatedOn()));
			ps.setString(4, c.getBody());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM JOBPOSTINGS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM JOBAPPS WHERE (AUTHOR_ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM JOBAPROFILES WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
}
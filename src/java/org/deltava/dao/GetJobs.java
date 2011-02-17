// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to read Job applications and profiles from the database.
 * @author Luke
 * @version 3.6
 * @since 3.4
 */

public class GetJobs extends DAO {

	/**
	 * Initializes the Data Access Object
	 * @param c the JDBC connection to use
	 */
	public GetJobs(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves a specific Job posting.
	 * @param id the database ID
	 * @return a JobPosting bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public JobPosting get(int id) throws DAOException {
		try {
			prepareStatement("SELECT * FROM JOBPOSTINGS WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query
			List<JobPosting> results = execute();
			if (results.isEmpty())
				return null;
			
			// Load applicatons
			JobPosting jp = results.get(0);
			prepareStatementWithoutLimits("SELECT JA.*, P.FIRSTNAME, P.LASTNAME FROM JOBAPPS JA, PILOTS P "
				+ "WHERE (JA.AUTHOR_ID=P.ID) AND (JA.ID=?) ORDER BY JA.CREATED");
			_ps.setInt(1, id);
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Application a = new Application(id, rs.getInt(2));
				a.setCreatedOn(rs.getTimestamp(3));
				a.setStatus(rs.getInt(4));
				a.setBody(rs.getString(5));
				a.setFirstName(rs.getString(6));
				a.setLastName(rs.getString(7));
				jp.add(a);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Load comments
			prepareStatementWithoutLimits("SELECT * FROM JOBCOMMENTS WHERE (ID=?) ORDER BY CREATED");
			_ps.setInt(1, id);
			rs = _ps.executeQuery();
			while (rs.next()) {
				Comment c = new Comment(id, rs.getInt(2));
				c.setCreatedOn(rs.getTimestamp(3));
				c.setBody(rs.getString(4));
				jp.add(c);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return jp;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Job postings.
	 * @return a List of JobPosting beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<JobPosting> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM JOBPOSTINGS ORDER BY CLOSES DESC, CREATED DESC");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all open Job postings.
	 * @return a List of JobPosting beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<JobPosting> getOpen() throws DAOException {
		try {
			prepareStatement("SELECT * FROM JOBPOSTINGS WHERE (STATUS=?) ORDER BY CLOSES DESC");
			_ps.setInt(1, JobPosting.OPEN);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all active Job postings.
	 * @return a List of JobPosting beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<JobPosting> getActive() throws DAOException {
		try {
			prepareStatement("SELECT * FROM JOBPOSTINGS WHERE (STATUS<>?) ORDER BY CLOSES DESC, CREATED DESC");
			_ps.setInt(1, JobPosting.COMPLETE);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Job posting result sets.
	 */
	private List<JobPosting> execute() throws SQLException {
		ResultSet rs = _ps.executeQuery();
		boolean hasAppCount = (rs.getMetaData().getColumnCount() > 11);
		
		// Execute the query
		List<JobPosting> results = new ArrayList<JobPosting>();
		while (rs.next()) {
			JobPosting jp = new JobPosting(rs.getString(7));
			jp.setID(rs.getInt(1));
			jp.setCreatedOn(rs.getTimestamp(2));
			jp.setClosesOn(rs.getTimestamp(3));
			jp.setHireManagerID(rs.getInt(4));
			jp.setStatus(rs.getInt(5));
			jp.setStaffOnly(rs.getBoolean(6));
			jp.setSummary(rs.getString(8));
			jp.setMinLegs(rs.getInt(9));
			jp.setMinAge(rs.getInt(10));
			jp.setDescription(rs.getString(11));
			if (hasAppCount)
				jp.setAppCount(rs.getInt(12));
			
			results.add(jp);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAcademyIssues extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyIssues(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Flight Academy Issue. <i>This loads the Issue Comments</i>.
	 * @param id the database ID
	 * @return an Issue bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Issue getIssue(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM ACADEMY_ISSUES WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Do the query and return the first result
			List<Issue> results = execute();
			if (results.isEmpty())
				return null;
			
			// Get the issue
			Issue i = results.get(0);
			
			// Load the comments
			prepareStatementWithoutLimits("SELECT * FROM ACADEMY_ISSUECOMMENTS WHERE (ID=?) ORDER BY CREATED_ON");
			_ps.setInt(1, id);
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				IssueComment ic = new IssueComment(rs.getInt(2));
				ic.setCreatedOn(rs.getTimestamp(3));
				ic.setBody(rs.getString(4));
				i.addComment(ic);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return i;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getAll() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID), (SELECT AUTHOR FROM ACADEMY_ISSUECOMMENTS IC WHERE "
					+ "(I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM ACADEMY_ISSUES I LEFT JOIN "
					+ "ACADEMY_ISSUECOMMENTS IC ON (I.ID=IC.ID) GROUP BY I.ID ORDER BY I.CREATED_ON");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	public Collection<Issue> getByPilot(int id) throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID), (SELECT AUTHOR FROM ACADEMY_ISSUECOMMENTS IC WHERE "
					+ "(I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM ACADEMY_ISSUES I LEFT JOIN "
					+ "ACADEMY_ISSUECOMMENTS IC ON (I.ID=IC.ID) WHERE (I.ISPUBLIC=?) OR (I.AUTHOR=?) OR (I.ASSIGNEDTO=?) "
					+ "GROUP BY I.ID ORDER BY I.CREATED_ON");
			_ps.setBoolean(1, true);
			_ps.setInt(2, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all active Flight Academy Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getActive() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID), (SELECT AUTHOR FROM ACADEMY_ISSUECOMMENTS IC WHERE "
					+ "(I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM ACADEMY_ISSUES I LEFT JOIN "
					+ "ACADEMY_ISSUECOMMENTS IC ON (I.ID=IC.ID) WHERE (I.STATUS=?) GROUP BY I.ID ORDER BY I.CREATED_ON");
			_ps.setInt(1, Issue.OPEN);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Issue result sets.
	 */
	private List<Issue> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasCount = (rs.getMetaData().getColumnCount() > 10);
		
		// Load results
		List<Issue> results = new ArrayList<Issue>();
		while (rs.next()) {
			Issue i = new Issue(rs.getString(8));
			i.setID(rs.getInt(1));
			i.setAuthorID(rs.getInt(2));
			i.setAssignedTo(rs.getInt(3));
			i.setCreatedOn(rs.getTimestamp(4));
			i.setResolvedOn(rs.getTimestamp(5));
			i.setStatus(rs.getInt(6));
			i.setPublic(rs.getBoolean(7));
			i.setBody(rs.getString(9));
			if (hasCount) {
				i.setCommentCount(rs.getInt(10));
				i.setLastCommentAuthorID(rs.getInt(11));
			}
			
			// Add to results
			results.add(i);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
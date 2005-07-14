// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

/**
 * A Data Access object to retrieve Issues and Issue Comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetIssue extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection
	 */
	public GetIssue(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Issue and its comments.
	 * @param id the Issue ID
	 * @return the Issue
	 * @throws DAOException if a JDBC error occurs
	 */
	public Issue get(int id) throws DAOException {
		try {
			prepareStatement("SELECT * FROM ISSUES WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query - return null if nothing found
			List results = execute();
			if (results.size() == 0)
				return null;
			
			// Populate the bean
			Issue i = (Issue) results.get(0);
			
			// Get comments and return
			getComments(i);
			return i;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues.
	 * @param sortBy the column to sort the results using
	 * @return a List of Issues
	 * @throws DAOException the a JDBC error occurs
	 */
	public List getAll(String sortBy) throws DAOException {

		try {
			prepareStatement("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC  FROM ISSUES I LEFT JOIN "
			      + "ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) GROUP BY I.ID ORDER BY " + sortBy);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues that have a user as the Author or Assignee.
	 * @param id the database ID of the User
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getUserIssues(int id) throws DAOException {
		try {
			prepareStatement("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC FROM ISSUES I LEFT JOIN "
			      + "ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE ((I.AUTHOR=?) OR (I.ASSIGNEDTO=?)) GROUP BY I.ID");
			_ps.setInt(1, id);
			_ps.setInt(2, id);
			
			// Execute the query and return
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues with a particular status code.
	 * @param status the status code
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByStatus(int status) throws DAOException {
		try {
			prepareStatement("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC FROM ISSUES I LEFT JOIN "
			      + "ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE (I.STATUS=?) GROUP BY I.ID ORDER BY I.ID");
			_ps.setInt(1, status);
			
			// Execute the query and return
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to return all comments for a particular issue.
	 */
	private void getComments(Issue i) throws SQLException {
		prepareStatementWithoutLimits("SELECT ID, AUTHOR, CREATED, COMMENTS FROM ISSUE_COMMENTS WHERE (ISSUE_ID=?) "
		      + "ORDER BY CREATED");
		_ps.setInt(1, i.getID());
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			IssueComment ic = new IssueComment(rs.getInt(1), rs.getString(4));
			ic.setIssueID(i.getID());
			ic.setCreatedBy(rs.getInt(2));
			ic.setCreatedOn(rs.getTimestamp(3));
			
			// add to Issue
			i.addComment(ic);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
	}
	
	private List execute() throws SQLException {

		List results = new ArrayList();
		
		// Execute the result
		ResultSet rs = _ps.executeQuery();
		ResultSetMetaData md = rs.getMetaData();
		
		while (rs.next()) {
			Issue i = new Issue(rs.getInt(1), rs.getString(6));
			i.setCreatedBy(rs.getInt(2));
			i.setAssignedTo(rs.getInt(3));
			i.setCreatedOn(rs.getTimestamp(4));
			i.setResolvedOn(rs.getTimestamp(5));
			i.setDescription(rs.getString(7));
			i.setArea(rs.getInt(8));
			i.setPriority(rs.getInt(9));
			i.setStatus(rs.getInt(10));
			i.setType(rs.getInt(11));
			i.setMajorVersion(rs.getInt(12));
			i.setMinorVersion(rs.getInt(13));
			
			// Check if we have a column 14/15 for last comment date & comment count
			if (md.getColumnCount() > 13) {
				i.setLastCommentOn(rs.getTimestamp(14));
				i.setCommentCount(rs.getInt(15));
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
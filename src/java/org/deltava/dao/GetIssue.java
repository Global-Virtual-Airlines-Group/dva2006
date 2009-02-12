// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

/**
 * A Data Access object to retrieve Issues and Issue Comments.
 * @author Luke
 * @version 2.4
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
			prepareStatement("SELECT * FROM common.ISSUES WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query - return null if nothing found
			List<Issue> results = execute();
			if (results.size() == 0)
				return null;
			
			// Populate the bean, get comments and return
			Issue i = results.get(0);
			getComments(i);
			return i;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues.
	 * @param sortBy the column to sort the results using
	 * @param area the area code, or -1 if none
	 * @return a List of Issues
	 * @throws DAOException the a JDBC error occurs
	 */
	public List<Issue> getAll(String sortBy, int area) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC "
				+ "FROM common.ISSUES I LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID)");
		if (area >= 0)
			sqlBuf.append(" WHERE (I.AREA=?)");
		sqlBuf.append(" GROUP BY I.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			if (area >= 0)
				_ps.setInt(1, area);
				
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
	public List<Issue> getUserIssues(int id) throws DAOException {
		try {
			prepareStatement("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC FROM common.ISSUES I "
			      + "LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE ((I.AUTHOR=?) OR "
			      + "(I.ASSIGNEDTO=?)) AND ((I.STATUS=?) OR (I.STATUS=?)) GROUP BY I.ID ORDER BY I.STATUS, LC DESC");
			_ps.setInt(1, id);
			_ps.setInt(2, id);
			_ps.setInt(3, Issue.STATUS_OPEN);
			_ps.setInt(4, Issue.STATUS_DEFERRED);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues with a particular status code.
	 * @param status the status code
	 * @param area the area code, or -1 if none
	 * @param sortType the SQL sorting fragment
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Issue> getByStatus(int status, int area, String sortType) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC FROM "
				+ "common.ISSUES I LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE (I.STATUS=?)");
		if (area >= 0)
			sqlBuf.append(" AND (I.AREA=?)");
		sqlBuf.append(" GROUP BY I.ID ORDER BY ");
		sqlBuf.append(sortType);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, status);
			if (area >= 0)
				_ps.setInt(2, area);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Searches all Issues for a particular phrase.
	 * @param searchStr the search phrase
	 * @param status the status code, or -1 if none
	 * @param area the area code, or -1 if none
	 * @param includeComments TRUE if Issue Comments should be searched, otherwise FALSE
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Issue> search(String searchStr, int status, int area, boolean includeComments) throws DAOException {
	
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(IC.ID) AS CC "
				+ "FROM common.ISSUES I LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) "
				+ "WHERE ((LOCATE(?, I.DESCRIPTION) > 0)");
		sqlBuf.append(includeComments ? " OR (LOCATE(?, IC.COMMENTS) > 0))" : ")");
		if (status >= 0)
			sqlBuf.append(" AND (I.STATUS=?)");
		if (area >= 0)
			sqlBuf.append(" AND (I.AREA=?)");
			
		sqlBuf.append(" GROUP BY I.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, searchStr);
			int pos = 1;
			if (includeComments)
				_ps.setString(++pos, searchStr);
			if (status >= 0)
				_ps.setInt(++pos, status);
			if (area >= 0)
				_ps.setInt(++pos, area);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to return all comments for a particular issue.
	 */
	private void getComments(Issue i) throws SQLException {
		prepareStatementWithoutLimits("SELECT ID, AUTHOR, CREATED, COMMENTS FROM common.ISSUE_COMMENTS "
		      + "WHERE (ISSUE_ID=?) ORDER BY CREATED");
		_ps.setInt(1, i.getID());
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			IssueComment ic = new IssueComment(rs.getInt(1), rs.getString(4));
			ic.setIssueID(i.getID());
			ic.setAuthorID(rs.getInt(2));
			ic.setCreatedOn(rs.getTimestamp(3));
			
			// add to Issue
			i.addComment(ic);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
	}
	
	private List<Issue> execute() throws SQLException {
		
		// Execute the result
		ResultSet rs = _ps.executeQuery();
		ResultSetMetaData md = rs.getMetaData();
		List<Issue> results = new ArrayList<Issue>();
		while (rs.next()) {
			Issue i = new Issue(rs.getInt(1), rs.getString(6));
			i.setAuthorID(rs.getInt(2));
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
			i.setSecurity(rs.getInt(14));
			
			// Check if we have a column 15/16 for last comment date & comment count
			if (md.getColumnCount() > 15) {
				i.setLastCommentOn(rs.getTimestamp(15));
				i.setCommentCount(rs.getInt(16));
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
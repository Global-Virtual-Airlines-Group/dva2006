// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.help.*;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to load Online Help and Help Desk entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetHelp extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetHelp(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Help Desk Issue. <i>This loads the Issue Comments</i>.
	 * @param id the database ID
	 * @return an Issue bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Issue getIssue(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM HELPDESK WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);

			// Do the query and return the first result
			List<Issue> results = executeIssue();
			if (results.isEmpty())
				return null;

			// Get the issue
			Issue i = results.get(0);

			// Load the comments
			prepareStatementWithoutLimits("SELECT * FROM HELPDESK_COMMENTS WHERE (ID=?) ORDER BY CREATED_ON");
			_ps.setInt(1, id);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					IssueComment ic = new IssueComment(rs.getInt(2));
					ic.setCreatedOn(rs.getTimestamp(3).toInstant());
					ic.setFAQ(rs.getBoolean(4));
					ic.setBody(rs.getString(5));
					i.addComment(ic);
				}
			}

			_ps.close();
			return i;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Help Desk Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getAll() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID), MAX(IC.CREATED_ON), (SELECT AUTHOR FROM "
					+ "HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM "
					+ "HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) GROUP BY I.ID ORDER BY I.CREATED_ON");
			return executeIssue();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Help Desk Issue authors.
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getAuthors() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT AUTHOR FROM HELPDESK");
			Collection<Integer> results = new HashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Help Desk Issue assignees.
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getAssignees() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT ASSIGNEDTO FROM HELPDESK WHERE (ASSIGNEDTO<>0)");
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Help Desk Issues for a Pilot, or Public Issues.
	 * @param authorID the author's database ID
	 * @param assigneeID the assignee's database ID
	 * @param showPublic TRUE to show public issues, otherwise FALSE
	 * @param activeOnly TRUE to show active issues only, otherwise FALSE
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getByPilot(int authorID, int assigneeID, boolean showPublic, boolean activeOnly) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, COUNT(IC.ID), MAX(IC.CREATED_ON), (SELECT AUTHOR "
				+ "FROM HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC "
				+ "FROM HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) WHERE ((I.AUTHOR=?) OR "
				+ "(I.ASSIGNEDTO=?) ");
		if (showPublic)
			sqlBuf.append("OR (I.ISPUBLIC=?)");
		sqlBuf.append(") ");
		if (activeOnly)
			sqlBuf.append("AND (I.STATUS<>?) ");

		sqlBuf.append("GROUP BY I.ID ORDER BY I.STATUS, I.CREATED_ON");

		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());
			_ps.setInt(++pos, authorID);
			_ps.setInt(++pos, assigneeID);
			if (showPublic)
				_ps.setBoolean(++pos, true);
			if (activeOnly)
				_ps.setInt(++pos, Issue.CLOSED);

			return executeIssue();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Help Desk Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getActive() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID), MAX(IC.CREATED_ON), (SELECT AUTHOR FROM "
					+ "HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM "
					+ "HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) WHERE (I.STATUS=?) GROUP BY "
					+ "I.ID ORDER BY I.CREATED_ON");
			_ps.setInt(1, Issue.OPEN);
			return executeIssue();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Help Desk FAQ Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getFAQ() throws DAOException {
		try {
			prepareStatement("SELECT * FROM HELPDESK WHERE (ISFAQ=?)");
			_ps.setBoolean(1, true);
			Map<Integer, Issue> results = CollectionUtils.createMap(executeIssue(), "ID");

			// Build the FAQ answer comments query
			if (!results.isEmpty()) {
				StringBuilder sqlBuf = new StringBuilder("SELECT * FROM HELPDESK_COMMENTS WHERE ID IN (");
				for (Iterator<Integer> i = results.keySet().iterator(); i.hasNext();) {
					Integer id = i.next();
					sqlBuf.append(id.toString());
					if (i.hasNext())
						sqlBuf.append(',');
				}

				// Execute the Query
				sqlBuf.append(')');
				prepareStatementWithoutLimits(sqlBuf.toString());
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next()) {
						IssueComment ic = new IssueComment(rs.getInt(2));
						ic.setID(rs.getInt(1));
						ic.setCreatedOn(rs.getTimestamp(3).toInstant());
						ic.setFAQ(rs.getBoolean(4));
						ic.setBody(rs.getString(5));

						// Stuff into issue
						Issue i = results.get(Integer.valueOf(ic.getID()));
						if (i != null)
						i.addComment(ic);
					}
				}

				_ps.close();
			}

			return results.values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches all Issues for a particular phrase.
	 * @param searchStr the search phrase
	 * @param includeComments TRUE if Issue Comments should be searched, otherwise FALSE
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Issue> search(String searchStr, boolean includeComments) throws DAOException {
	
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED_ON) AS LC, COUNT(IC.ID) AS CC FROM "
				+ "HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) WHERE ((LOCATE(?, I.SUBJECT) > 0) "
				+ "OR (LOCATE(?, I.BODY) > 0)");
		if (includeComments)
			sqlBuf.append(" OR (LOCATE(?, IC.BODY) > 0)");
		sqlBuf.append(") GROUP BY I.ID ORDER BY I.ISFAQ DESC, I.STATUS DESC, I.CREATED_ON");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, searchStr);
			_ps.setString(2, searchStr);
			if (includeComments)
				_ps.setString(3, searchStr);
			
			return executeIssue();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Issue result sets.
	 */
	private List<Issue> executeIssue() throws SQLException {
		List<Issue> results = new ArrayList<Issue>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasCount = (rs.getMetaData().getColumnCount() > 12);
			while (rs.next()) {
				Issue i = new Issue(rs.getString(9));
				i.setID(rs.getInt(1));
				i.setAuthorID(rs.getInt(2));
				i.setAssignedTo(rs.getInt(3));
				i.setCreatedOn(rs.getTimestamp(4).toInstant());
				i.setResolvedOn(toInstant(rs.getTimestamp(5)));
				i.setStatus(rs.getInt(6));
				i.setPublic(rs.getBoolean(7));
				i.setFAQ(rs.getBoolean(8));
				i.setBody(rs.getString(10));
				if (hasCount) {
					i.setCommentCount(rs.getInt(11));
					i.setLastComment(toInstant(rs.getTimestamp(12)));
					i.setLastCommentAuthorID(rs.getInt(13));
				}

				results.add(i);
			}
		}

		_ps.close();
		return results;
	}
}
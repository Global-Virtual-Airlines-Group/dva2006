// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.help.*;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to load Online Help and Help Desk entries.
 * @author Luke
 * @version 1.0
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
	 * Returns a particular Online Help Entry.
	 * @param id the entry title
	 * @return a HelpEntry bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public OnlineHelpEntry get(String id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM HELP WHERE (ID=?)");
			_ps.setString(1, id);

			// Execute the query, return first result
			List<OnlineHelpEntry> results = executeHelp();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Online Help Entries.
	 * @return a Collection of OnlineHelpEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<OnlineHelpEntry> getOnlineHelp() throws DAOException {
		try {
			prepareStatement("SELECT * FROM HELP ORDER BY ID");
			return executeHelp();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a particular Help Desk Issue. <i>This loads the Issue Comments</i>.
	 * @param id the database ID
	 * @return an Issue bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Issue getIssue(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM HELPDESK WHERE (ID=?)");
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
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				IssueComment ic = new IssueComment(rs.getInt(2));
				ic.setCreatedOn(rs.getTimestamp(3));
				ic.setFAQ(rs.getBoolean(4));
				ic.setBody(rs.getString(5));
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
	 * Returns all Help Desk Issues for a Pilot, or Public Issues.
	 * @param id the Pilot's database ID
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getByPilot(int id, boolean showPublic) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, COUNT(IC.ID), MAX(IC.CREATED_ON), (SELECT AUTHOR "
				+ "FROM HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC "
				+ "FROM HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) WHERE (I.AUTHOR=?) OR "
				+ "(I.ASSIGNEDTO=?) ");
		if (showPublic)
			sqlBuf.append("OR (I.ISPUBLIC=?) ");

		sqlBuf.append("GROUP BY I.ID ORDER BY I.STATUS, I.CREATED_ON");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);
			_ps.setInt(2, id);
			if (showPublic)
				_ps.setBoolean(3, true);

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
				sqlBuf.append(")");
				prepareStatementWithoutLimits(sqlBuf.toString());
				ResultSet rs = _ps.executeQuery();
				while (rs.next()) {
					IssueComment ic = new IssueComment(rs.getInt(2));
					ic.setID(rs.getInt(1));
					ic.setCreatedOn(rs.getTimestamp(3));
					ic.setFAQ(rs.getBoolean(4));
					ic.setBody(rs.getString(5));

					// Stuff into issue
					Issue i = results.get(new Integer(ic.getID()));
					if (i != null)
						i.addComment(ic);
				}

				// Clean up and return
				rs.close();
				_ps.close();
			}
			
			return results.values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse the result set.
	 */
	private List<OnlineHelpEntry> executeHelp() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<OnlineHelpEntry> results = new ArrayList<OnlineHelpEntry>();
		while (rs.next())
			results.add(new OnlineHelpEntry(rs.getString(1), rs.getString(2)));

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to parse Issue result sets.
	 */
	private List<Issue> executeIssue() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasCount = (rs.getMetaData().getColumnCount() > 12);

		// Load results
		List<Issue> results = new ArrayList<Issue>();
		while (rs.next()) {
			Issue i = new Issue(rs.getString(9));
			i.setID(rs.getInt(1));
			i.setAuthorID(rs.getInt(2));
			i.setAssignedTo(rs.getInt(3));
			i.setCreatedOn(rs.getTimestamp(4));
			i.setResolvedOn(rs.getTimestamp(5));
			i.setStatus(rs.getInt(6));
			i.setPublic(rs.getBoolean(7));
			i.setFAQ(rs.getBoolean(8));
			i.setBody(rs.getString(10));
			if (hasCount) {
				i.setCommentCount(rs.getInt(11));
				i.setLastComment(rs.getTimestamp(12));
				i.setLastCommentAuthorID(rs.getInt(13));
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
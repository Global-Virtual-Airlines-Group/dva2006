// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2016, 2017, 2019, 2020, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.help.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load Online Help and Help Desk entries.
 * @author Luke
 * @version 11.0
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
		Issue i = null;
		try {
			try (PreparedStatement ps = prepareWithoutLimits("SELECT H.*, HL.ISSUE_ID FROM HELPDESK H LEFT JOIN HELPDESK_LINKS HL ON (H.ID=HL.ID) WHERE (H.ID=?) LIMIT 1")) {
				ps.setInt(1, id);
				i = executeIssue(ps).stream().findFirst().orElse(null);
				if (i == null) return null;
			}

			// Load the comments
			try (PreparedStatement ps = prepareWithoutLimits("SELECT IC.*, IFS.NAME, IFS.SIZE FROM HELPDESK_COMMENTS IC LEFT JOIN HELPDESK_FILES IFS ON ((IC.ID=IFS.ID) AND (IC.CREATED_ON=IFS.CREATED_ON)) WHERE (IC.ID=?) ORDER BY IC.CREATED_ON")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						IssueComment ic = new IssueComment(rs.getInt(2));
						ic.setCreatedOn(rs.getTimestamp(3).toInstant());
						ic.setFAQ(rs.getBoolean(4));
						ic.setBody(rs.getString(5));
						ic.setName(rs.getString(6));
						ic.setSize(rs.getInt(7));
						i.addComment(ic);
					}
				}
			}

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
		try (PreparedStatement ps = prepare("SELECT I.*, HL.ISSUE_ID, COUNT(IC.ID), MAX(IC.CREATED_ON), (SELECT AUTHOR FROM HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM "
			+ "HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) LEFT JOIN HELPDESK_LINKS HL ON (I.ID=HL.ID) GROUP BY I.ID ORDER BY I.CREATED_ON DESC")) {
			return executeIssue(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads an attached File.
	 * @param id the issue database ID
	 * @param createdOn the comment creation epoch timestamp
	 * @return the file data
	 * @throws DAOException if a JDBC error occurs
	 */
	public IssueComment getFile(int id, java.time.Instant createdOn) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT HDC.AUTHOR, HDF.SIZE, HDF.NAME, HDF.BODY FROM HELPDESK_COMMENTS HDC LEFT JOIN HELPDESK_FILES HDF ON (HDC.ID=HDF.ID) AND (HDC.CREATED_ON=HDF.CREATED_ON) "
			+ "WHERE (HDC.ID=?) AND (HDC.CREATED_ON=?) LIMIT 1")) {
			ps.setInt(1, id);
			ps.setTimestamp(2, createTimestamp(createdOn));
			
			IssueComment ic = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ic = new IssueComment(rs.getInt(1));
					ic.setID(id);
					ic.setCreatedOn(createdOn);
					ic.setSize(rs.getInt(2));
					ic.setName(rs.getString(3));
					ic.load(rs.getBytes(4));
				}
			}
			
			return ic;
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT AUTHOR FROM HELPDESK")) {
			Collection<Integer> results = new HashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT ASSIGNEDTO FROM HELPDESK WHERE (ASSIGNEDTO<>0)")) {
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
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
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, HL.ISSUE_ID, COUNT(IC.ID), MAX(IC.CREATED_ON) AS LCD, (SELECT AUTHOR FROM HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC "
			+ "FROM HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) LEFT JOIN HELPDESK_LINKS HL ON (I.ID=HL.ID) WHERE ((I.AUTHOR=?) OR (I.ASSIGNEDTO=?) ");
		if (showPublic)
			sqlBuf.append("OR (I.ISPUBLIC=?)");
		sqlBuf.append(") ");
		if (activeOnly)
			sqlBuf.append("AND (I.STATUS<>?) ");

		sqlBuf.append("GROUP BY I.ID ORDER BY I.STATUS, IFNULL(LCD, I.CREATED_ON) DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int pos = 0;
			ps.setInt(++pos, authorID);
			ps.setInt(++pos, assigneeID);
			if (showPublic) ps.setBoolean(++pos, true);
			if (activeOnly) ps.setInt(++pos, IssueStatus.CLOSED.ordinal());
			return executeIssue(ps);
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
		try (PreparedStatement ps = prepare("SELECT I.*, HL.ISSUE_ID, COUNT(IC.ID), MAX(IC.CREATED_ON) AS LCD, (SELECT AUTHOR FROM HELPDESK_COMMENTS IC WHERE (I.ID=IC.ID) ORDER BY IC.CREATED_ON DESC LIMIT 1) AS LC FROM "
			+ "HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) LEFT JOIN HELPDESK_LINKS HL ON (I.ID=HL.ID) WHERE ((I.STATUS=?) OR (I.STATUS=?)) GROUP BY I.ID ORDER BY IFNULL(LCD, I.CREATED_ON) DESC")) {
			ps.setInt(1, IssueStatus.OPEN.ordinal());
			ps.setInt(2, IssueStatus.ASSIGNED.ordinal());
			return executeIssue(ps);
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
			Map<Integer, Issue> results = new HashMap<Integer, Issue>();
			try (PreparedStatement ps = prepare("SELECT I.*, HL.ISSUE_ID FROM HELPDESK I LEFT JOIN HELPDESK_LINKS HL ON (I.ID=HL.ID) WHERE (I.ISFAQ=?)")) {
				ps.setBoolean(1, true);
				results.putAll(CollectionUtils.createMap(executeIssue(ps), Issue::getID));
			}

			// Build the FAQ answer comments query
			if (!results.isEmpty()) {
				StringBuilder sqlBuf = new StringBuilder("SELECT * FROM HELPDESK_COMMENTS WHERE ID IN (");
				sqlBuf.append(StringUtils.listConcat(results.keySet(), ","));
				sqlBuf.append(')');

				// Execute the Query
				try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
					try (ResultSet rs = ps.executeQuery()) {
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
				}
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
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, HL.ISSUE_ID, MAX(IC.CREATED_ON) AS LC, COUNT(IC.ID) AS CC FROM HELPDESK I LEFT JOIN HELPDESK_COMMENTS IC ON (I.ID=IC.ID) LEFT JOIN HELPDESK_LINKS HL ON (I.ID=HL.ID) "
			+ "WHERE ((LOCATE(?, I.SUBJECT) > 0) OR (LOCATE(?, I.BODY) > 0)");
		if (includeComments)
			sqlBuf.append(" OR (LOCATE(?, IC.BODY) > 0)");
		sqlBuf.append(") GROUP BY I.ID ORDER BY I.ISFAQ DESC, I.STATUS DESC, I.CREATED_ON");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, searchStr);
			ps.setString(2, searchStr);
			if (includeComments)
				ps.setString(3, searchStr);
			
			return executeIssue(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Issue result sets.
	 */
	private static List<Issue> executeIssue(PreparedStatement ps) throws SQLException {
		List<Issue> results = new ArrayList<Issue>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasCount = (rs.getMetaData().getColumnCount() > 13);
			while (rs.next()) {
				Issue i = new Issue(rs.getString(9));
				i.setID(rs.getInt(1));
				i.setAuthorID(rs.getInt(2));
				i.setAssignedTo(rs.getInt(3));
				i.setCreatedOn(rs.getTimestamp(4).toInstant());
				i.setResolvedOn(toInstant(rs.getTimestamp(5)));
				i.setStatus(IssueStatus.values()[rs.getInt(6)]);
				i.setPublic(rs.getBoolean(7));
				i.setFAQ(rs.getBoolean(8));
				i.setBody(rs.getString(10));
				i.setLinkedIssueID(rs.getInt(11));
				if (hasCount) {
					i.setCommentCount(rs.getInt(12));
					i.setLastComment(toInstant(rs.getTimestamp(13)));
					i.setLastCommentAuthorID(rs.getInt(14));
				}

				results.add(i);
			}
		}

		return results;
	}
}
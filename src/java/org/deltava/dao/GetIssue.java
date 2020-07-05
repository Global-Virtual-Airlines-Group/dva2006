	// Copyright 2005, 2006, 2009, 2011, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.system.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access object to retrieve Issues and Issue Comments.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT * FROM common.ISSUES WHERE (ID=?)")) {
			ps.setInt(1, id);
			
			// Execute the query - return null if nothing found
			List<Issue> results = execute(ps);
			if (results.size() == 0) return null;
			
			// Populate the bean, get comments and return
			Issue i = results.get(0);
			loadComments(i);
			loadAirlines(i);
			return i;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues.
	 * @param sortBy the column to sort the results using
	 * @param area the IssueArea or null if none
	 * @param airlineCode the airline code or null for all
	 * @return a List of Issues
	 * @throws DAOException the a JDBC error occurs
	 */
	public List<Issue> getAll(String sortBy, IssueArea area, String airlineCode) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(DISTINCT IC.ID) AS CC FROM common.ISSUES I LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) LEFT JOIN "
			+ "common.ISSUE_AIRLINES IA ON (I.ID=IA.ID) WHERE (IA.AIRLINE LIKE ?)");
		if (area != null)
			sqlBuf.append(" AND (I.AREA=?)");
		
		sqlBuf.append(" GROUP BY I.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, (airlineCode == null) ? "%" : airlineCode);
			if (area != null) ps.setInt(2, area.ordinal());
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(DISTINCT IC.ID) AS CC FROM common.ISSUES I LEFT JOIN common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE ((I.AUTHOR=?) OR "
		      + "(I.ASSIGNEDTO=?)) AND ((I.STATUS=?) OR (I.STATUS=?)) GROUP BY I.ID ORDER BY I.STATUS, LC DESC")) {
			ps.setInt(1, id);
			ps.setInt(2, id);
			ps.setInt(3, IssueStatus.OPEN.ordinal());
			ps.setInt(4, IssueStatus.DEFERRED.ordinal());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Issues with a particular status.
	 * @param status the IssueStatus
	 * @param area the IssueArea or null if none
	 * @param sortType the SQL sorting fragment
	 * @param airlineCode the airline code or null for all
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Issue> getByStatus(IssueStatus status, IssueArea area, String sortType, String airlineCode) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(DISTINCT IC.ID) AS CC FROM common.ISSUES I LEFT JOIN common.ISSUE_AIRLINES IA ON (I.ID=IA.ID) LEFT JOIN "
			+ "common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE (I.STATUS=?) AND (IA.AIRLINE LIKE ?)");
		if (area != null)
			sqlBuf.append(" AND (I.AREA=?)");
		sqlBuf.append(" GROUP BY I.ID ORDER BY ");
		sqlBuf.append(sortType);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, status.ordinal());
			ps.setString(2, (airlineCode == null) ? "%" : airlineCode);
			if (area != null) ps.setInt(3, area.ordinal());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Searches all Issues for a particular phrase.
	 * @param searchStr the search phrase
	 * @param status the IssueStatus or null if none
	 * @param area the IssueArea or null if none
	 * @param airlineCode the airline code or null for all
	 * @param includeComments TRUE if Issue Comments should be searched, otherwise FALSE
	 * @return a List of Issues
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Issue> search(String searchStr, IssueStatus status, IssueArea area, String airlineCode, boolean includeComments) throws DAOException {
	
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, MAX(IC.CREATED) AS LC, COUNT(DISTINCT IC.ID) AS CC FROM common.ISSUES I LEFT JOIN common.ISSUE_AIRLINES IA ON (I.ID=IA.ID) LEFT JOIN "
			+ "common.ISSUE_COMMENTS IC ON (I.ID=IC.ISSUE_ID) WHERE ((LOCATE(?, I.DESCRIPTION) > 0)");
		sqlBuf.append(includeComments ? " OR (LOCATE(?, IC.COMMENTS) > 0))" : ")");
		if (status != null)
			sqlBuf.append(" AND (I.STATUS=?)");
		if (area != null)
			sqlBuf.append(" AND (I.AREA=?)");
			
		sqlBuf.append(" AND (IA.AIRLINE LIKE ?) GROUP BY I.ID");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, searchStr);
			int pos = 1;
			if (includeComments) ps.setString(++pos, searchStr);
			if (status != null) ps.setInt(++pos, status.ordinal());
			if (area != null) ps.setInt(++pos, area.ordinal());
			ps.setString(++pos, (airlineCode == null) ? "%" : airlineCode);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads an attached File.
	 * @param fileID the file database ID
	 * @return the file data
	 * @throws DAOException if a JDBC error occurs
	 */
	public IssueComment getFile(int fileID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SIZE, NAME, BODY FROM common.ISSUE_FILES WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, fileID);
			
			// Execute the query
			IssueComment ic = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ic = new IssueComment(fileID, "");
					ic.setSize(rs.getInt(1));
					ic.setName(rs.getString(2));
					ic.load(rs.getBytes(3));
				}
			}
			
			return ic;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to return all comments for a particular issue.
	 */
	private void loadComments(Issue i) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT IC.ID, IC.AUTHOR, IC.CREATED, IC.COMMENTS, IFNULL(IFL.SIZE, -1), IFL.NAME FROM common.ISSUE_COMMENTS IC LEFT JOIN common.ISSUE_FILES IFL "
			+ "ON (IC.ID=IFL.ID) WHERE (IC.ISSUE_ID=?) ORDER BY IC.CREATED")) {
			ps.setInt(1, i.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					IssueComment ic = new IssueComment(rs.getInt(1), rs.getString(4));
					ic.setParentID(i.getID());
					ic.setAuthorID(rs.getInt(2));
					ic.setCreatedOn(rs.getTimestamp(3).toInstant());
					int size = rs.getInt(5);
					if (size > 0) {
						ic.setSize(size);
						ic.setName(rs.getString(6));
					}
			
					i.add(ic);
				}
			}
		}
	}

	/*
	 * Helper method to load airline/issue mappings.
	 */
	private void loadAirlines(Issue i) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRLINE FROM common.ISSUE_AIRLINES WHERE (ID=?)")) {
			ps.setInt(1, i.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					i.addAirline(SystemData.getApp(rs.getString(1)));
			}
		}
	}
	
	/*
	 * Helper method to parse Issue result sets.
	 */
	private static List<Issue> execute(PreparedStatement ps) throws SQLException {
		List<Issue> results = new ArrayList<Issue>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasLastComment = (rs.getMetaData().getColumnCount() > 15);
			while (rs.next()) {
				Issue i = new Issue(rs.getInt(1), rs.getString(6));
				i.setAuthorID(rs.getInt(2));
				i.setAssignedTo(rs.getInt(3));
				i.setCreatedOn(rs.getTimestamp(4).toInstant());
				i.setResolvedOn(toInstant(rs.getTimestamp(5)));
				i.setDescription(rs.getString(7));
				i.setArea(IssueArea.values()[rs.getInt(8)]);
				i.setPriority(IssuePriority.values()[rs.getInt(9)]);
				i.setStatus(IssueStatus.values()[rs.getInt(10)]);
				i.setType(Issue.IssueType.values()[rs.getInt(11)]);
				i.setMajorVersion(rs.getInt(12));
				i.setMinorVersion(rs.getInt(13));
				i.setSecurity(IssueSecurity.values()[rs.getInt(14)]);
				if (hasLastComment) {
					i.setLastCommentOn(toInstant(rs.getTimestamp(15)));
					i.setCommentCount(rs.getInt(16));
				}

				results.add(i);
			}
		}

		return results;
	}
}
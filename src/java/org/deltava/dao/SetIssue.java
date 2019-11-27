// Copyright 2005, 2006, 2007, 2009, 2011, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to update development Issues.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetIssue extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetIssue(Connection c) {
		super(c);
	}

	/*
	 * Helper method to initialize the prepared statement for INSERTs.
	 */
	private void insert(Issue i) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.ISSUES (ID, AUTHOR, ASSIGNEDTO, CREATED, RESOLVED, SUBJECT, "
			+ "DESCRIPTION, AREA, PRIORITY, STATUS, TYPE, SECURITY, MAJOR, MINOR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, i.getID());
				ps.setInt(2, i.getAuthorID());
				ps.setInt(3, i.getAssignedTo());
				ps.setTimestamp(4, createTimestamp(i.getCreatedOn()));
				ps.setTimestamp(5, (i.getStatus() == Issue.STATUS_OPEN) ? null : createTimestamp(i.getResolvedOn()));
				ps.setString(6, i.getSubject());
				ps.setString(7, i.getDescription());
				ps.setInt(8, i.getArea());
				ps.setInt(9, i.getPriority());
				ps.setInt(10, i.getStatus());
				ps.setInt(11, i.getType());
				ps.setInt(12, i.getSecurity());
				ps.setInt(13, i.getMajorVersion());
				ps.setInt(14, i.getMinorVersion());
				executeUpdate(ps, 1);
		}
		
		i.setID(getNewID());
	}

	/*
	 * Helper method to initialize the prepared statement for UPDATEs.
	 */
	private void update(Issue i) throws SQLException {
		try (PreparedStatement ps = prepare("UPDATE common.ISSUES SET ASSIGNEDTO=?, RESOLVED=?, SUBJECT=?, DESCRIPTION=?, "
				+ "AREA=?, PRIORITY=?, STATUS=?, TYPE=?, SECURITY=?, MAJOR=?, MINOR=? WHERE (ID=?)")) {
			ps.setInt(1, i.getAssignedTo());
			ps.setTimestamp(2, (i.getStatus() == Issue.STATUS_OPEN) ? null : createTimestamp(i.getResolvedOn()));
			ps.setString(3, i.getSubject());
			ps.setString(4, i.getDescription());
			ps.setInt(5, i.getArea());
			ps.setInt(6, i.getPriority());
			ps.setInt(7, i.getStatus());
			ps.setInt(8, i.getType());
			ps.setInt(9, i.getSecurity());
			ps.setInt(10, i.getMajorVersion());
			ps.setInt(11, i.getMinorVersion());
			ps.setInt(12, i.getID());
			executeUpdate(ps, 1);
		}
	}

	/**
	 * Writes an Issue to the database. This can both update and create issues. New Issue comments are also wrtten.
	 * @param i the Issue to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Issue i) throws DAOException {
		try {
			startTransaction();
			
			// Initialize the prepared statement depending on the type of operation we are doing
			if (i.getID() == 0)
				insert(i);
			else
				update(i);

			// If we wrote a new Issue, don't bother with the comments but get the issue ID
			if (i.getID() != 0) {
				for (IssueComment ic : i.getComments()) {
					if (ic.getID() == 0)
						write(ic);
				}
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Issue comment to the database.
	 * @param ic the IssueComment to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(IssueComment ic) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.ISSUE_COMMENTS (ISSUE_ID, AUTHOR, CREATED, COMMENTS) VALUES (?, ?, NOW(), ?)")) {
				ps.setInt(1, ic.getIssueID());
				ps.setInt(2, ic.getAuthorID());
				ps.setString(3, ic.getComments());
				executeUpdate(ps, 1);
			}
			
			ic.setID(getNewID());
			
			// Write file if necessary
			if (ic.isLoaded()) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.ISSUE_FILES (ID, SIZE, NAME, BODY) VALUES (?, ?, ?, ?)")) {
					ps.setInt(1, ic.getID());
					ps.setInt(2, ic.getSize());
					ps.setString(3, ic.getName());
					ps.setBinaryStream(4, ic.getInputStream(), ic.getSize());
					executeUpdate(ps, 1);
				}
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
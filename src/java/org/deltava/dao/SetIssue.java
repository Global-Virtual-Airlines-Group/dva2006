// Copyright 2005, 2006, 2007, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to update development Issues.
 * @author Luke
 * @version 4.1
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

	/**
	 * Helper method to initialize the prepared statement for INSERTs.
	 */
	private void insert(Issue i) throws SQLException {
		prepareStatement("INSERT INTO common.ISSUES (ID, AUTHOR, ASSIGNEDTO, CREATED, RESOLVED, SUBJECT, "
				+ "DESCRIPTION, AREA, PRIORITY, STATUS, TYPE, MAJOR, MINOR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		// Populate the prepared statement
		_ps.setInt(1, i.getID());
		_ps.setInt(2, i.getAuthorID());
		_ps.setInt(3, i.getAssignedTo());
		_ps.setTimestamp(4, createTimestamp(i.getCreatedOn()));
		_ps.setTimestamp(5, (i.getStatus() == Issue.STATUS_OPEN) ? null : createTimestamp(i.getResolvedOn()));
		_ps.setString(6, i.getSubject());
		_ps.setString(7, i.getDescription());
		_ps.setInt(8, i.getArea());
		_ps.setInt(9, i.getPriority());
		_ps.setInt(10, i.getStatus());
		_ps.setInt(11, i.getType());
		_ps.setInt(12, i.getMajorVersion());
		_ps.setInt(13, i.getMinorVersion());
	}

	/**
	 * Helper method to initialize the prepared statement for UPDATEs.
	 */
	private void update(Issue i) throws SQLException {
		prepareStatement("UPDATE common.ISSUES SET ASSIGNEDTO=?, RESOLVED=?, SUBJECT=?, DESCRIPTION=?, "
				+ "AREA=?, PRIORITY=?, STATUS=?, TYPE=?, SECURITY=?, MAJOR=?, MINOR=? WHERE (ID=?)");

		// Populate the prepared statement
		_ps.setInt(1, i.getAssignedTo());
		_ps.setTimestamp(2, (i.getStatus() == Issue.STATUS_OPEN) ? null : createTimestamp(i.getResolvedOn()));
		_ps.setString(3, i.getSubject());
		_ps.setString(4, i.getDescription());
		_ps.setInt(5, i.getArea());
		_ps.setInt(6, i.getPriority());
		_ps.setInt(7, i.getStatus());
		_ps.setInt(8, i.getType());
		_ps.setInt(9, i.getSecurity());
		_ps.setInt(10, i.getMajorVersion());
		_ps.setInt(11, i.getMinorVersion());
		_ps.setInt(12, i.getID());
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

			executeUpdate(1);

			// If we wrote a new Issue, don't bother with the comments but get the issue ID
			if (i.getID() == 0)
				i.setID(getNewID());
			else {
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
			prepareStatementWithoutLimits("INSERT INTO common.ISSUE_COMMENTS (ISSUE_ID, AUTHOR, CREATED, "
					+ "COMMENTS) VALUES (?, ?, NOW(), ?)");
			_ps.setInt(1, ic.getIssueID());
			_ps.setInt(2, ic.getAuthorID());
			_ps.setString(3, ic.getComments());
			executeUpdate(1);
			ic.setID(getNewID());
			
			// Write file if necessary
			if (ic.isLoaded()) {
				prepareStatementWithoutLimits("INSERT INTO common.ISSUE_FILES (ID, SIZE, NAME, BODY) VALUES (?, ?, ?, ?)");
				_ps.setInt(1, ic.getID());
				_ps.setInt(2, ic.getSize());
				_ps.setString(3, ic.getName());
				_ps.setBinaryStream(4, ic.getInputStream(), ic.getSize());
				executeUpdate(1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
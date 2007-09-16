// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.help.*;

/**
 * A Data Access Object to update Online Help entries and Help Desk Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetHelp extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetHelp(Connection c) {
		super(c);
	}

	/**
	 * Writes an Online Help Entry to the database.
	 * @param entry the HelpEntry bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OnlineHelpEntry entry) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO HELP (ID, SUBJECT, BODY) VALUES (?, ?, ?)");
			_ps.setString(1, entry.getTitle());
			_ps.setString(2, entry.getSubject());
			_ps.setString(3, entry.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates a Help Desk Issue in the database.
	 * @param i the Issue bean
	 * @throws DAOException if a JDBC error occurss
	 */
	public void write(Issue i) throws DAOException {
		try {
			if (i.getID() == 0) {
				prepareStatement("INSERT INTO HELPDESK (AUTHOR, ASSIGNEDTO, CREATED_ON, STATUS, ISPUBLIC, "
						+ "SUBJECT, BODY) VALUES (?, ?, ?, ?, ?, ?, ?)");
				_ps.setInt(1, i.getAuthorID());
				_ps.setInt(2, i.getAssignedTo());
				_ps.setTimestamp(3, createTimestamp(i.getCreatedOn()));
				_ps.setInt(4, i.getStatus());
				_ps.setBoolean(5, i.getPublic());
				_ps.setString(6, i.getSubject());
				_ps.setString(7, i.getBody());
			} else {
				prepareStatement("UPDATE HELPDESK SET STATUS=?, ASSIGNEDTO=?, RESOLVED_ON=?, ISPUBLIC=?, "
						+ "SUBJECT=?, BODY=? WHERE (ID=?)");
				_ps.setInt(1, i.getStatus());
				_ps.setInt(2, i.getAssignedTo());
				_ps.setTimestamp(3, createTimestamp(i.getResolvedOn()));
				_ps.setBoolean(4, i.getPublic());
				_ps.setString(5, i.getSubject());
				_ps.setString(6, i.getBody());
				_ps.setInt(7, i.getID());
			}

			// Update the issue
			executeUpdate(1);
			if (i.getID() == 0)
				i.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Adds an Issue Comment to the database.
	 * @param ic the IssueComment bean
	 * @throws DAOException if a JDBC error occurss
	 */
	public void write(IssueComment ic) throws DAOException {
		try {
			prepareStatement("INSERT INTO HELPDESK_COMMENTS (ID, AUTHOR, CREATED_ON, BODY) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, ic.getID());
			_ps.setInt(2, ic.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(ic.getCreatedOn()));
			_ps.setString(4, ic.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a Help Desk Issue as part of the FAQ.
	 * @param id the Issue database ID
	 * @param createdOn the creation date of the Comment to mark as the answer, or null to remove from the FAQ
	 * @throws DAOException if a JDBC error occurs
	 */
	public void markFAQ(int id, java.util.Date createdOn) throws DAOException {
		try {
			startTransaction();
			
			// Clear the answer
			prepareStatementWithoutLimits("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (ISFAQ=?)");
			_ps.setBoolean(1, false);
			_ps.setInt(2, id);
			_ps.setBoolean(3, true);
			executeUpdate(0);
			
			// Update the FAQ
			prepareStatement("UPDATE HELPDESK SET ISFAQ=? WHERE (ID=?)");
			_ps.setBoolean(1, (createdOn != null));
			_ps.setInt(2, id);
			executeUpdate(1);
			
			// Set the FAQ answer, or clear the flag
			if (createdOn != null) {
				prepareStatement("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (CREATED_ON=?)");
				_ps.setBoolean(1, true);
				_ps.setInt(2, id);
				_ps.setTimestamp(3, createTimestamp(createdOn));
				executeUpdate(1);
			} else {
				prepareStatementWithoutLimits("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (ISFAQ=?)");
				_ps.setBoolean(1, false);
				_ps.setInt(2, id);
				_ps.setBoolean(3, true);
				executeUpdate(0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Help Desk Issue from the database.
	 * @param id the Issue database ID
	 * @throws DAOException if a JDBC error occurss
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM HELPDESK WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Help Desk Issue comment.
	 * @param issueID the Issue's database ID
	 * @param createdOn the Comment's creation date/time
	 * @throws DAOException if a JDBC error occurs 
	 */
	public void deleteComment(int issueID, java.util.Date createdOn) throws DAOException {
		try {
			startTransaction();
			
			// Remove it from the FAQ if this is the FAQ answer
			prepareStatement("SELECT COUNT(*) FROM HELPDESK_COMMENTS WHERE (ID=?) AND (ISFAQ=?) AND (CREATED_ON=?)");
			_ps.setInt(1, issueID);
			_ps.setBoolean(2, true);
			_ps.setTimestamp(3, createTimestamp(createdOn));
			
			// Do the check
			ResultSet rs = _ps.executeQuery();
			boolean isFAQAnswer = rs.next() ? (rs.getInt(1) == 1) : false;
			rs.close();
			
			// Delete the comment
			prepareStatementWithoutLimits("DELETE FROM HELPDESK_COMMENTS WHERE (ID=?) AND (CREATED_ON=?)");
			_ps.setInt(1, issueID);
			_ps.setTimestamp(2, createTimestamp(createdOn));
			executeUpdate(1);
			
			// If we are the FAQ answer, clear the FAQ flag from the Issue
			if (isFAQAnswer) {
				prepareStatementWithoutLimits("UPDATE HELPDESK SET ISFAQ=? WHERE (ID=?)");
				_ps.setBoolean(1, false);
				_ps.setInt(2, issueID);
				executeUpdate(0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
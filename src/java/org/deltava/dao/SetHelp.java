// Copyright 2005, 2006, 2010, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.help.*;

/**
 * A Data Access Object to update Online Help entries and Help Desk Issues.
 * @author Luke
 * @version 9.0
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
	 * Writes a Help Desk response template to the database.
	 * @param rsp the ResponseTemplate bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(ResponseTemplate rsp) throws DAOException {
		try (PreparedStatement ps = prepare("REPLACE INTO HELPDESK_RSPTMP (TITLE, BODY) VALUES (?, ?)")) {
			ps.setString(1, rsp.getTitle());
			ps.setString(2, rsp.getBody());
			executeUpdate(ps, 1);
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
				try (PreparedStatement ps = prepare("INSERT INTO HELPDESK (AUTHOR, ASSIGNEDTO, CREATED_ON, STATUS, ISPUBLIC, SUBJECT, BODY) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
					ps.setInt(1, i.getAuthorID());
					ps.setInt(2, i.getAssignedTo());
					ps.setTimestamp(3, createTimestamp(i.getCreatedOn()));
					ps.setInt(4, i.getStatus());
					ps.setBoolean(5, i.getPublic());
					ps.setString(6, i.getSubject());
					ps.setString(7, i.getBody());
					executeUpdate(ps, 1);
				}
				
				i.setID(getNewID());
			} else {
				try (PreparedStatement ps = prepare("UPDATE HELPDESK SET STATUS=?, ASSIGNEDTO=?, RESOLVED_ON=?, ISPUBLIC=?, SUBJECT=?, BODY=? WHERE (ID=?)")) {
					ps.setInt(1, i.getStatus());
					ps.setInt(2, i.getAssignedTo());
					ps.setTimestamp(3, createTimestamp(i.getResolvedOn()));
					ps.setBoolean(4, i.getPublic());
					ps.setString(5, i.getSubject());
					ps.setString(6, i.getBody());
					ps.setInt(7, i.getID());
					executeUpdate(ps, 1);
				}
			}
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
		try  {
			startTransaction();

			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO HELPDESK_COMMENTS (ID, AUTHOR, CREATED_ON, BODY) VALUES (?, ?, ?, ?)")) {
				ps.setInt(1, ic.getID());
				ps.setInt(2, ic.getAuthorID());
				ps.setTimestamp(3, createTimestamp(ic.getCreatedOn()));
				ps.setString(4, ic.getBody());
				executeUpdate(ps, 1);
			}
			
			if (ic.isLoaded()) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO HELPDESK_FILES (ID, CREATED_ON, SIZE, NAME, BODY) VALUES (?, ?, ?, ?, ?)")) {
					ps.setInt(1, ic.getID());
					ps.setTimestamp(2, createTimestamp(ic.getCreatedOn()));
					ps.setInt(3, ic.getSize());
					ps.setString(4, ic.getName());
					ps.setBinaryStream(5, ic.getInputStream(), ic.getSize());
					executeUpdate(ps, 1);
				}
			}
		
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a Help Desk Issue as part of the FAQ.
	 * @param id the Issue database ID
	 * @param createdOn the creation date of the Comment to mark as the answer, or null to remove from the FAQ
	 * @throws DAOException if a JDBC error occurs
	 */
	public void markFAQ(int id, java.time.Instant createdOn) throws DAOException {
		try {
			startTransaction();
			
			// Clear the answer
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (ISFAQ=?)")) {
				ps.setBoolean(1, false);
				ps.setInt(2, id);
				ps.setBoolean(3, true);
				executeUpdate(ps, 0);
			}
			
			// Update the FAQ
			try (PreparedStatement ps = prepare("UPDATE HELPDESK SET ISFAQ=? WHERE (ID=?)")) {
				ps.setBoolean(1, (createdOn != null));
				ps.setInt(2, id);
				executeUpdate(ps, 1);
			}
			
			// Set the FAQ answer, or clear the flag
			if (createdOn != null) {
				try (PreparedStatement ps = prepare("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (CREATED_ON=?)")) {
					ps.setBoolean(1, true);
					ps.setInt(2, id);
					ps.setTimestamp(3, createTimestamp(createdOn));
					executeUpdate(ps, 1);
				}
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE HELPDESK_COMMENTS SET ISFAQ=? WHERE (ID=?) AND (ISFAQ=?)")) {
					ps.setBoolean(1, false);
					ps.setInt(2, id);
					ps.setBoolean(3, true);
					executeUpdate(ps, 0);
				}
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM HELPDESK WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Help Desk response template from the database.
	 * @param title the template title
	 * @throws DAOException if a JDBC error occurss
	 */
	public void deleteTemplate(String title) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM HELPDESK_RSPTMP WHERE (TITLE=?)")) {
			ps.setString(1, title);
			executeUpdate(ps, 0);
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
	public void deleteComment(int issueID, java.time.Instant createdOn) throws DAOException {
		try {
			startTransaction();
			
			// Remove it from the FAQ if this is the FAQ answer
			boolean isFAQAnswer = false;
			try (PreparedStatement ps = prepare("SELECT COUNT(*) FROM HELPDESK_COMMENTS WHERE (ID=?) AND (ISFAQ=?) AND (CREATED_ON=?)")) {
				ps.setInt(1, issueID);
				ps.setBoolean(2, true);
				ps.setTimestamp(3, createTimestamp(createdOn));
				try (ResultSet rs = ps.executeQuery()) {
					isFAQAnswer = rs.next() && (rs.getInt(1) > 0);
				}
			}
			
			// Delete the comment
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM HELPDESK_COMMENTS WHERE (ID=?) AND (CREATED_ON=?)")) {
				ps.setInt(1, issueID);
				ps.setTimestamp(2, createTimestamp(createdOn));
				executeUpdate(ps, 1);
			}
			
			// If we are the FAQ answer, clear the FAQ flag from the Issue
			if (isFAQAnswer) {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE HELPDESK SET ISFAQ=? WHERE (ID=?)")) {
					ps.setBoolean(1, false);
					ps.setInt(2, issueID);
					executeUpdate(ps, 0);
				}
			}
			
			commitTransaction();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
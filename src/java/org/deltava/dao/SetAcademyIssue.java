// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to update Flight Academy Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetAcademyIssue extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAcademyIssue(Connection c) {
		super(c);
	}

	/**
	 * Updates a Flight Academy Issue in the database.
	 * @param i the Issue bean
	 * @throws DAOException if a JDBC error occurss
	 */
	public void write(Issue i) throws DAOException {
		try {
			if (i.getID() == 0) {
				prepareStatement("INSERT INTO ACADEMY_ISSUES (AUTHOR, CREATED_ON, STATUS, SUBJECT, BODY) VALUES "
						+ "(?, ?, ?, ?, ?)");
				_ps.setInt(1, i.getAuthorID());
				_ps.setTimestamp(2, createTimestamp(i.getCreatedOn()));
				_ps.setInt(3, i.getStatus());
				_ps.setString(4, i.getSubject());
				_ps.setString(5, i.getBody());
			} else {
				prepareStatement("UPDATE ACADEMY_ISSUES SET STATUS=?, SUBJECT=?, BODY=? WHERE (ID=?)");
				_ps.setInt(1, i.getStatus());
				_ps.setString(2, i.getSubject());
				_ps.setString(3, i.getBody());
				_ps.setInt(4, i.getID());
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
			prepareStatement("INSERT INTO ACADEMY_ISSUECOMMENTS (ID, AUTHOR, CREATED_ON, BODY) VALUES (?, ?, ?, ?)");
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
	 * Deletes a Flight Academy Issue from the database.
	 * @param id the Issue database ID
	 * @throws DAOException if a JDBC error occurss
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM ACADEMY_ISSUES WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
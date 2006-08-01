// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.Iterator;
import java.sql.*;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

/**
 * A Data Access Object to update Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetIssue extends DAO {
    
    /**
     * Initializes the Data Access Object.
     * @param c the JDBC connection
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
              + "AREA=?, PRIORITY=?, STATUS=?, TYPE=?, MAJOR=?, MINOR=? WHERE (ID=?)");

        // Populate the prepared statement
        _ps.setInt(1, i.getAssignedTo());
        _ps.setTimestamp(2, (i.getStatus() == Issue.STATUS_OPEN) ? null : createTimestamp(i.getResolvedOn()));
        _ps.setString(3, i.getSubject());
        _ps.setString(4, i.getDescription());
        _ps.setInt(5, i.getArea());
        _ps.setInt(6, i.getPriority());
        _ps.setInt(7, i.getStatus());
        _ps.setInt(8, i.getType());
        _ps.setInt(9, i.getMajorVersion());
        _ps.setInt(10, i.getMinorVersion());
        _ps.setInt(11, i.getID());
    }

    /**
     * Writes an Issue to the database. This can both update and create issues. New Issue comments are also wrtten.
     * @param i the Issue to write
     * @throws DAOException if a JDBC error occurs
     */
    public void write(Issue i) throws DAOException {
        
        try {
            // Initialize the prepared statement depending on the type of operation we are doing
            if (i.getID() == 0) {
                insert(i);
            } else {
                update(i);
            }
            
            // Update the database
			executeUpdate(1);
            
            // If we wrote a new Issue, don't bother with the comments but get the issue ID
            if (i.getID() == 0) {
                i.setID(getNewID());
                return;
            }
            
            // Parse through the comments
            for (Iterator it = i.getComments().iterator(); it.hasNext(); ) {
                IssueComment ic = (IssueComment) it.next();
                if (ic.getID() == 0)
                    writeComment(ic);
            }
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Writes an Issue comment to the database. This can both update and create comments.
     * @param ic the IssueComment to write
     * @throws DAOException if a JDBC error occurs
     */
    public void writeComment(IssueComment ic) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("REPLACE INTO common.ISSUE_COMMENTS (ID, ISSUE_ID, AUTHOR, CREATED, "
    		      + "COMMENTS) VALUES (?, ?, ?, ?, ?)");
    		
    		_ps.setInt(1, ic.getID());
    		_ps.setInt(2, ic.getIssueID());
            _ps.setInt(3, ic.getAuthorID());
            _ps.setTimestamp(4, createTimestamp(ic.getCreatedOn()));
            _ps.setString(5, ic.getComments());

            // Write the new comments
			executeUpdate(1);
			
			// Update the issue comment ID if inserting a new record
			ic.setID(getNewID());
        } catch (SQLException se) {
            throw new DAOException(se);
    	}
    }
}
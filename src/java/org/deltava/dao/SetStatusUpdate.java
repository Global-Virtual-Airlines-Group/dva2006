// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.StatusUpdate;

/**
 * A Data Access Object to write status updates for a Pilot to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetStatusUpdate extends DAO {
   
   private long _lastCreatedOn = 0;
   
    /**
     * Initializes the Data Access Object.
     * @param c the JDBC connection to use
     */
    public SetStatusUpdate(Connection c) {
        super(c);
    }

    /**
     * Writes the Status Update log entry to the database.
     * @param update the Status Update entry
     * @throws DAOException if a JDBC error occurs
     */
    public void write(StatusUpdate update) throws DAOException {
       
       // Check that the CREATED date is unique
       long cd = update.getCreatedOn().getTime(); 
       if (cd <= _lastCreatedOn) {
          cd = ++_lastCreatedOn;
       } else {
          _lastCreatedOn = cd;
       }
       
        try {
           // Prepare the statement and write
            prepareStatementWithoutLimits("INSERT INTO STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, "
                  + "TYPE, REMARKS) VALUES (?, ?, ?, ?, ?)");
            _ps.setInt(1, update.getID());
            _ps.setInt(2, update.getAuthorID());
            _ps.setLong(3, cd);
            _ps.setInt(4, update.getType());
            _ps.setString(5, update.getDescription());
            executeUpdate(1);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Writes a number of Status Update entries to the database.
     * @param updates a List of StatusUpdates
     * @throws DAOException if a JDBC error occurs
     */
    public void write(List updates) throws DAOException {
    	for (Iterator i = updates.iterator(); i.hasNext(); ) {
    		StatusUpdate upd = (StatusUpdate) i.next();
    		write(upd);
    	}
    }
}
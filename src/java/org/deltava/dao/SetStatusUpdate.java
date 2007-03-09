// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.StatusUpdate;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write status updates for a Pilot to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetStatusUpdate extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetStatusUpdate(Connection c) {
		super(c);
	}
	
	/**
	 * Writes the Status Update log entry to <i>the current database</i>.
	 * @param update the Status Update entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(StatusUpdate update) throws DAOException {
	   write(SystemData.get("airline.db"), update);
	}

	/**
	 * Writes the Status Update log entry to a database.
	 * @param dbName the database name
	 * @param update the Status Update entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(String dbName, StatusUpdate update) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
	   sqlBuf.append(formatDBName(dbName));
	   sqlBuf.append(".STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, TYPE, REMARKS) VALUES (?, ?, ?, ?, ?) ON "
			   +"DUPLICATE KEY UPDATE CREATED=DATE_ADD(CREATED, INTERVAL 1 SECOND)");
	   
		try {
			// Prepare the statement and write
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, update.getID());
			_ps.setInt(2, update.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(update.getCreatedOn()));
			_ps.setInt(4, update.getType());
			_ps.setString(5, update.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a number of Status Update entries to the database.
	 * @param updates a Collection of StatusUpdates
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<StatusUpdate> updates) throws DAOException {
		for (Iterator<StatusUpdate> i = updates.iterator(); i.hasNext();) {
			StatusUpdate upd = i.next();
			write(SystemData.get("airline.db"), upd);
		}
	}
}
// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.StatusUpdate;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write status updates for a Pilot to the database.
 * @author Luke
 * @version 2.8
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
		try {
			prepareStatementWithoutLimits("INSERT INTO STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, TYPE, REMARKS) "
				+ "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE CREATED=DATE_ADD(CREATED, INTERVAL 1 SECOND)");
			
			long lastUpdateTime = 0;
			for (Iterator<StatusUpdate> i = updates.iterator(); i.hasNext();) {
				StatusUpdate upd = i.next();
				if (upd.getCreatedOn().getTime() <= lastUpdateTime)
					upd.setCreatedOn(new java.util.Date(lastUpdateTime + 1000));
					
				lastUpdateTime = upd.getCreatedOn().getTime();
				
				// Write the data
				_ps.setInt(1, upd.getID());
				_ps.setInt(2, upd.getAuthorID());
				_ps.setTimestamp(3, createTimestamp(upd.getCreatedOn()));
				_ps.setInt(4, upd.getType());
				_ps.setString(5, upd.getDescription());
				_ps.addBatch();
			}

			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
	}
	
	/**
	 * Clears spurious Leave of Absence records created within 24 hours if a Pilot
	 * returns from an LOA immediately.
	 * @param id the Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearLOA(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM STATUS_UPDATES WHERE (PILOT_ID=?) AND (TYPE=?) AND "
				+ "(CREATED > DATE_SUB(NOW(), INTERVAL 24 HOUR))");
			_ps.setInt(1, id);
			_ps.setInt(2, StatusUpdate.LOA);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
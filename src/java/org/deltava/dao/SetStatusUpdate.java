// Copyright 2005, 2006, 2007, 2009, 2010, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write status updates for a Pilot to the database.
 * @author Luke
 * @version 8.7
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
	   sqlBuf.append(".STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, TYPE, REMARKS) VALUES (?, ?, ?, ?, ?)");
	   
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, update.getID());
			_ps.setInt(2, update.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(update.getDate()));
			_ps.setInt(4, update.getType().ordinal());
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
		if (updates.isEmpty())
			return;
		
		try {
			startTransaction();
			prepareStatementWithoutLimits("INSERT INTO STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, TYPE, REMARKS) VALUES (?, ?, ?, ?, ?)");
			
			long lastUpdateTime = 0;
			for (StatusUpdate upd : updates) {
				if (upd.getDate().toEpochMilli() <= lastUpdateTime)
					upd.setDate(upd.getDate().plusMillis(2));
					
				lastUpdateTime = upd.getDate().toEpochMilli();
				
				// Write the data
				_ps.setInt(1, upd.getID());
				_ps.setInt(2, upd.getAuthorID());
				_ps.setTimestamp(3, createTimestamp(upd.getDate()));
				_ps.setInt(4, upd.getType().ordinal());
				_ps.setString(5, upd.getDescription());
				_ps.addBatch();
			}

			executeBatchUpdate(1, updates.size());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
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
			prepareStatement("DELETE FROM STATUS_UPDATES WHERE (PILOT_ID=?) AND (TYPE=?) AND (CREATED > DATE_SUB(NOW(), INTERVAL 24 HOUR))");
			_ps.setInt(1, id);
			_ps.setInt(2, UpdateType.LOA.ordinal());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
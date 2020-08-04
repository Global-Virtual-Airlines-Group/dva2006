// Copyright 2005, 2006, 2007, 2009, 2010, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write status updates for a Pilot to the database.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class SetStatusUpdate extends DAO {
	
	private class UpdateComparator implements Comparator<StatusUpdate> {

		@Override
		public int compare(StatusUpdate upd1, StatusUpdate upd2) {
			return upd1.getDate().compareTo(upd2.getDate());
		}
	}

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
	   
	   try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, update.getID());
			ps.setInt(2, update.getAuthorID());
			ps.setTimestamp(3, createTimestamp(update.getDate()));
			ps.setInt(4, update.getType().ordinal());
			ps.setString(5, update.getDescription());
			executeUpdate(ps, 1);
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
		if (updates.isEmpty()) return;
		
		// Sort by date to ensure no duplicate dates
		List<StatusUpdate> upds = new ArrayList<StatusUpdate>(updates);
		upds.sort(new UpdateComparator());
		
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO STATUS_UPDATES (PILOT_ID, AUTHOR_ID, CREATED, TYPE, REMARKS) VALUES (?, ?, ?, ?, ?)")) {
				Instant lastUpdateTime = Instant.MIN;
				for (StatusUpdate upd : upds) {
					Instant updDate = upd.getDate().truncatedTo(ChronoUnit.MILLIS);
					if (!updDate.isAfter(lastUpdateTime))
						updDate = lastUpdateTime.plusMillis(5);
					
					lastUpdateTime = updDate;
					upd.setDate(updDate);
				
					// Write the data
					ps.setInt(1, upd.getID());
					ps.setInt(2, upd.getAuthorID());
					ps.setTimestamp(3, createTimestamp(upd.getDate()));
					ps.setInt(4, upd.getType().ordinal());
					ps.setString(5, upd.getDescription());
					ps.addBatch();
				}

				executeUpdate(ps, 1, updates.size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Clears spurious Leave of Absence records created within 24 hours if a Pilot returns from an LOA immediately.
	 * @param id the Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearLOA(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM STATUS_UPDATES WHERE (PILOT_ID=?) AND (TYPE=?) AND (CREATED > DATE_SUB(NOW(), INTERVAL 24 HOUR))")) {
			ps.setInt(1, id);
			ps.setInt(2, UpdateType.LOA.ordinal());
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
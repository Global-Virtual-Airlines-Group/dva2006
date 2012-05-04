// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * A Data Access Object to purge ACARS data.
 * @author Luke
 * @version 4.2
 * @since 3.2
 */

public class SetACARSPurge extends SetACARSLog {
	
	private static final Logger log = Logger.getLogger(SetACARSPurge.class);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSPurge(Connection c) {
		super(c);
	}

	/**
	 * Deletes ACARS text messages older than a specified number of hours.
	 * @param hours the number of hours
	 * @return the number of messages purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeMessages(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.MESSAGES WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes unfiled ACARS flight information older than a specified number of hours.
	 * @param hours the number of hours
	 * @param activeIDs a Collection of active Flight IDs
	 * @return a Collection of purged Flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> purgeFlights(int hours, Collection<Integer> activeIDs) throws DAOException {
		try {
			startTransaction();
			
			// Get IDs to purge
			prepareStatementWithoutLimits("SELECT F.ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCHER FD ON "
					+ "(F.ID=FD.ID) WHERE (F.PIREP=?) AND (F.ARCHIVED=?) AND (F.CREATED < DATE_SUB(NOW(), "
					+ "INTERVAL ? HOUR)) AND (IFNULL(FD.DISPATCHER_ID, ?)=?)");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, hours);
			_ps.setInt(4, 0);
			_ps.setInt(5, 0);
			
			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())	
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			// Clean up and remove active
			_ps.close();
			results.removeAll(activeIDs);

			// Purge the ones we want to
			for (Integer id : results) {
				log.info("Deleting Flight #" + id.toString());
				deleteInfo(id.intValue());
			}
			
			// Commit and return
			commitTransaction();
			return results;
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges old ACARS takeoff and landing logs older than a specified number of hours.
	 * @param hours the number of hours
	 * @return the number of entries purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeTakeoffs(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.TOLAND WHERE (EVENT_TIME < DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes position entries for a flight.
	 * @param flightID the flight database ID
	 * @return the number of entries deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int deletePositions(int flightID) throws DAOException {
		try {
			startTransaction();
			
			// Purge positions
			prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			int posCount = executeUpdate(0);
			
			// Purge ATC
			prepareStatementWithoutLimits("DELETE FROM acars.POSITION_ATC WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);
			
			commitTransaction();
			return posCount;
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to update or remove ACARS log entries.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class SetACARSLog extends DAO {
	
	private static final Logger log = Logger.getLogger(SetACARSData.class);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSLog(Connection c) {
		super(c);
	}

	/**
	 * Deletes an ACARS Connection log entry.
	 * @param id the connection ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteConnection(long id) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.CONS WHERE (ID=?)");
			_ps.setLong(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an ACARS Flight Information log entry, and associated position reports.
	 * @param flightID the flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteInfo(int flightID) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.FLIGHTS WHERE (ID=?) AND (ARCHIVED=?)");
			_ps.setInt(1, flightID);
			_ps.setBoolean(2, false);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
			prepareStatementWithoutLimits("SELECT F.ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON "
					+ "(F.ID=FD.ID) WHERE (F.PIREP=?) AND (F.ARCHIVED=?) AND (F.CREATED < DATE_SUB(NOW(), "
					+ "INTERVAL ? HOUR)) AND (IFNULL(FD.DISPATCHER_ID, ?)=?)");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, hours);
			_ps.setInt(4, 0);
			_ps.setInt(5, 0);
			
			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and remove active
			rs.close();
			_ps.close();
			results.removeAll(activeIDs);

			// Purge the ones we want to
			for (Iterator<Integer> i = results.iterator(); i.hasNext(); ) {
				Integer id = i.next();
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
	 * Purges old ACARS command logs from the database older than a specified number of hours.
	 * @param hours the number of hours
	 * @return the number of entries purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeLogs(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.COMMAND_STATS WHERE (CMDDATE < DATE_SUB(NOW(), "
					+ "INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks connections as completed.
	 * @param ids A collection of connection IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public void closeConnections(Collection<Long> ids) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE acars.CONS SET ENDDATE=NOW() WHERE (ID=?)");
			for (Iterator<Long> i = ids.iterator(); i.hasNext(); ) {
				long id = i.next().longValue();
				_ps.setLong(1, id);
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Moves ACARS position data from the live table to the archive.
	 * @param flightID the ACARS Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void archivePositions(int flightID) throws DAOException {
		try {
			startTransaction();

			// Copy the data to the archive
			prepareStatementWithoutLimits("INSERT INTO acars.POSITION_ARCHIVE SELECT P.* FROM acars.POSITIONS P "
					+ "WHERE (P.FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);

			// Delete the existing flight data
			prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);

			// Mark the flight as archived
			prepareStatement("UPDATE acars.FLIGHTS SET ARCHIVED=?, PIREP=? WHERE (ID=?)");
			_ps.setBoolean(1, true);
			_ps.setBoolean(2, true);
			_ps.setInt(3, flightID);
			executeUpdate(0);
			
			// Update archive log
			prepareStatementWithoutLimits("INSERT INTO acars.ARCHIVE_UPDATES (ID, ARCHIVED) VALUES (?, NOW()) ON "
					+" DUPLICATE KEY UPDATE ARCHIVED=NOW()");
			_ps.setInt(1, flightID);
			executeUpdate(0);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an ACARS Error message to the error log.
	 * @param err the Error bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logError(ACARSError err) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO acars.ERRORS (USERID, CREATED_ON, REMOTE_ADDR, REMOTE_HOST, "
					+ "CLIENT_BUILD, BETA, FS_VERSION, FSUIPC_VERSION, ERROR_MSG, STACKDUMP, STATEDATA) VALUES (?, ?, "
					+ "INET_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, err.getUserID());
			_ps.setTimestamp(2, createTimestamp(err.getCreatedOn()));
			_ps.setString(3, err.getRemoteAddr());
			_ps.setString(4, err.getRemoteHost());
			_ps.setInt(5, err.getClientBuild());
			_ps.setInt(6, err.getBeta());
			_ps.setInt(7, err.getFSVersion());
			_ps.setString(8, err.getFSUIPCVersion());
			_ps.setString(9, err.getMessage());
			_ps.setString(10, err.getStackDump());
			_ps.setString(11, err.getStateData());
			executeUpdate(1);
			
			// Get the new ID
			err.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an ACARS Error message from the error log.
	 * @param id the Error database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteError(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.ERRORS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Synchronizes ACARS position data to ensure that position reports from archived flights are stored within
	 * the corret table.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void synchronizeArchive() throws DAOException {
		try {
			// Get Flight IDs that haven't been copied
			prepareStatementWithoutLimits("SELECT DISTINCT P.FLIGHT_ID FROM acars.POSITIONS P, acars.FLIGHTS F WHERE "
					+ "(F.ARCHIVED=?) AND (P.FLIGHT_ID=F.ID)");
			_ps.setBoolean(1, true);
			
			// Copy the flight IDs
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				IDs.add(new Integer(rs.getInt(1)));
			
			// Clean up
			rs.close();
			_ps.close();
			
			// If we've got flights, copy them over
			if (!IDs.isEmpty()) {
				for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
					int id = i.next().intValue();
					startTransaction();
				
					// Copy the entries
					prepareStatementWithoutLimits("REPLACE INTO acars.POSITION_ARCHIVE (SELECT * FROM acars.POSITIONS WHERE "
							+ "(FLIGHT_ID=?))");
					_ps.setInt(1, id);
					int rowsMoved = executeUpdate(1);
				
					// Delete the entries
					prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
					_ps.setInt(1, id);
					executeUpdate(0);
					
					// Update archive log
					prepareStatementWithoutLimits("INSERT INTO acars.ARCHIVE_UPDATES (ID, ARCHIVED) VALUES (?, NOW()) ON "
							+" DUPLICATE KEY UPDATE ARCHIVED=NOW()");
					_ps.setInt(1, id);
					executeUpdate(0);
				
					// Commit and log
					commitTransaction();
					log.warn("Moved " + rowsMoved + " entries for Flight " + id + " to Position Archive");
				}
			}
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}
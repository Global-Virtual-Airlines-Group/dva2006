// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to update or remove ACARS log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetACARSLog extends DAO {

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
			prepareStatement("DELETE FROM acars.MESSAGES WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes unfiled ACARS flight information older than a specified number of hours.
	 * @param hours the number of hours
	 * @return the number of flights purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeFlights(int hours) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.FLIGHTS WHERE (PIREP=?) AND (ARCHIVED=?) AND (CREATED < "
					+ "DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, hours);
			return executeUpdate(0);
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
					+ "CLIENT_BUILD, FS_VERSION, FSUIPC_VERSION, ERROR_MSG, STACKDUMP, STATEDATA) VALUES (?, ?, "
					+ "INET_ATON(?), ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, err.getUserID());
			_ps.setTimestamp(2, createTimestamp(err.getCreatedOn()));
			_ps.setString(3, err.getRemoteAddr());
			_ps.setString(4, err.getRemoteHost());
			_ps.setInt(5, err.getClientBuild());
			_ps.setInt(6, err.getFSVersion());
			_ps.setString(7, err.getFSUIPCVersion());
			_ps.setString(8, err.getMessage());
			_ps.setString(9, err.getStackDump());
			_ps.setString(10, err.getStateData());
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
}
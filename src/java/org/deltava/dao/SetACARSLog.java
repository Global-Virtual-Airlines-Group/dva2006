// Copyright 2005, 2006, 2007, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to update or remove ACARS log entries.
 * @author Luke
 * @version 5.1
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
			prepareStatement("DELETE FROM acars.CONS WHERE (ID=CONV(?,10,16))");
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
			_ps.setInt(7, err.getSimulator().getCode());
			_ps.setString(8, err.getFSUIPCVersion());
			_ps.setString(9, err.getMessage());
			_ps.setString(10, err.getStackDump());
			_ps.setString(11, err.getStateData());
			executeUpdate(1);
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
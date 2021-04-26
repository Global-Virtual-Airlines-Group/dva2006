// Copyright 2005, 2006, 2007, 2009, 2010, 2012, 2013, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to update or remove ACARS log entries.
 * @author Luke
 * @version 10.0
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
		try (PreparedStatement ps = prepare("DELETE FROM acars.CONS WHERE (ID=CONV(?,10,16))")) {
			ps.setLong(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM acars.FLIGHTS WHERE (ID=?) AND (ARCHIVED=?)")) {
			ps.setInt(1, flightID);
			ps.setBoolean(2, false);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO acars.ERRORS (USERID, CREATED_ON, REMOTE_ADDR, REMOTE_HOST, CLIENT_BUILD, BETA, FS_VERSION, FSUIPC_VERSION, BRIDGE_VERSION, "
				+ "ISINFO, OS_VERSION, IS64BIT, CLR_VERSION, LOCALE, TZ, ERROR_MSG, STACKDUMP, STATEDATA, LOG) VALUES (?, ?, INET6_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, err.getAuthorID());
			ps.setTimestamp(2, createTimestamp(err.getCreatedOn()));
			ps.setString(3, err.getRemoteAddr());
			ps.setString(4, err.getRemoteHost());
			ps.setInt(5, err.getClientBuild());
			ps.setInt(6, err.getBeta());
			ps.setInt(7, err.getSimulator().getCode());
			ps.setString(8, err.getPluginVersion());
			ps.setString(9, err.getBridgeVersion());
			ps.setBoolean(10, err.getIsInfo());
			ps.setString(11, err.getOSVersion());
			ps.setBoolean(12, err.getIs64Bit());
			ps.setString(13, err.getCLRVersion());
			ps.setString(14, err.getLocale());
			ps.setString(15, err.getTimeZone());
			ps.setString(16, err.getMessage());
			ps.setString(17, err.getStackDump());
			ps.setString(18, err.getStateData());
			ps.setBinaryStream(19, err.getInputStream());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = 	prepareWithoutLimits("DELETE FROM acars.ERRORS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges all ACARS Error messages for a given build from the error log.
	 * @param build the client build
	 * @param beta the beta, zero for all betas and -1 for everything
	 * @return the number of reports deleted
	 * @throws DAOException
	 */
	public int purgeError(int build, int beta) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM acars.ERRORS WHERE (CLIENT_BUILD=?)");
		if (beta > 0)
			sqlBuf.append(" AND (BETA=?)");
		else if (beta == 0)
			sqlBuf.append(" AND (BETA<>?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, build);
			if (beta > -1)
				ps.setInt(2, beta);
			
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges all ACARS Error messages for a given user from the error log.
	 * @param userID the user's database ID
	 * @return the number of reports deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeByUser(int userID) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM acars.ERRORS WHERE (USERID=?)")) {
			ps.setInt(1, userID);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
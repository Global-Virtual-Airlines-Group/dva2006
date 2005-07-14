// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import javax.servlet.http.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.CommandLog;

/**
 * A Data Access Object to write system logging (user sessions, statistics) entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetSystemData extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSystemData(Connection c) {
		super(c);
	}

	/**
	 * Logs the creation of an HTTP Session.
	 * @param s the HTTP session
	 * @throws DAOException if a JDBC error occurs
	 */
	public void openSession(HttpSession s) throws DAOException {
		try {
			prepareStatement("INSERT INTO SYS_SESSIONS (ID, START_TIME) VALUES (?, ?)");
			_ps.setString(1, s.getId());
			_ps.setTimestamp(2, new Timestamp(s.getCreationTime()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Associates a user with a particular HTTP session.
	 * @param sessionID the Session ID
	 * @param usr the Pilot bean
	 * @param remoteAddr the Pilot's remote IP address
	 * @param remoteHost the Pilot's remote host name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateSession(String sessionID, Pilot usr, String remoteAddr, String remoteHost) throws DAOException {
		try {
			prepareStatement("UPDATE SYS_SESSIONS SET PILOT_ID=?, REMOTE_ADDR=?, REMOTE_HOST=? WHERE (ID=?)");
			_ps.setInt(1, usr.getID());
			_ps.setString(2, remoteAddr);
			_ps.setString(3, remoteHost);
			_ps.setString(4, sessionID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs the closing of an HTTP Session.
	 * @param sessionID the Session ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void closeSession(String sessionID) throws DAOException {
		try {
			prepareStatement("UPDATE SYS_SESSIONS SET END_TIME=? WHERE (ID=?)");
			_ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			_ps.setString(2, sessionID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs a Web Site Command invocation.
	 * @param log the Command log entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logCommand(CommandLog log) throws DAOException {
		try {
			prepareStatement("INSERT INTO SYS_COMMANDS (PILOT_ID, REMOTE_ADDR, REMOTE_HOST, NAME, RESULT, TOTAL_TIME, "
					+ "BE_TIME, SUCCESS) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, log.getPilotID());
			_ps.setString(2, log.getRemoteAddr());
			_ps.setString(3, log.getRemoteHost());
			_ps.setString(4, log.getName());
			_ps.setString(5, log.getResult());
			_ps.setInt(6, log.getTime());
			_ps.setInt(7, log.getBackEndTime());
			_ps.setBoolean(8, log.getSuccess());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
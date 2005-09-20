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
			prepareStatement("INSERT INTO SYS_SESSIONS (ID, START_TIME) VALUES (?, NOW())");
			_ps.setString(1, s.getId());
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
			prepareStatement("UPDATE SYS_SESSIONS SET PILOT_ID=?, REMOTE_ADDR=INET_ATON(?), "
					+ "REMOTE_HOST=? WHERE (ID=?)");
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
			prepareStatement("UPDATE SYS_SESSIONS SET END_TIME=NOW() WHERE (ID=?)");
			_ps.setString(1, sessionID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an HTTP Session log entry, for an anonymous user.
	 * @param sessionID the Session ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteSession(String sessionID) throws DAOException {
	   try {
	      prepareStatement("DELETE FROM SYS_SESSIONS WHERE (ID=?)");
	      _ps.setString(1, sessionID);
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
			prepareStatement("INSERT INTO SYS_COMMANDS (CMDDATE, PILOT_ID, REMOTE_ADDR, REMOTE_HOST, "
					+ "NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS) VALUES (?, ?, INET_ATON(?), ?, ?, ?, ?, ?, ?)");
			_ps.setTimestamp(1, createTimestamp(log.getDate()));
			_ps.setInt(2, log.getPilotID());
			_ps.setString(3, log.getRemoteAddr());
			_ps.setString(4, log.getRemoteHost());
			_ps.setString(5, log.getName());
			_ps.setString(6, log.getResult());
			_ps.setInt(7, log.getTime());
			_ps.setInt(8, log.getBackEndTime());
			_ps.setBoolean(9, log.getSuccess());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs the execution time of a Scheduled Task.
	 * @param name the Scheduled Task name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logTaskExecution(String name) throws DAOException {
	   try {
	      prepareStatement("REPLACE INTO SYS_TASKS (ID, LASTRUN) VALUES (?, NOW())");
	      _ps.setString(1, name);
	      executeUpdate(1);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
}
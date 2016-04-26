// Copyright 2005, 2006, 2007, 2008, 2009, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;
import java.time.zone.ZoneRules;

import org.deltava.beans.TZInfo;
import org.deltava.beans.servlet.CommandLog;

/**
 * A Data Access Object to write system logging (user commands, tasks) entries.
 * @author Luke
 * @version 7.0
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
	 * Logs Web Site Command invocation.
	 * @param entries the Command log entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logCommands(Collection<CommandLog> entries) throws DAOException {
		try {
			prepareStatement("INSERT INTO SYS_COMMANDS (CMDDATE, PILOT_ID, REMOTE_ADDR, REMOTE_HOST, "
					+ "NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS) VALUES (?, ?, INET6_ATON(?), ?, ?, ?, ?, ?, ?) ON "
					+ "DUPLICATE KEY UPDATE CMDDATE=?");
			
			// Write the log entries
			for (Iterator<CommandLog> i = entries.iterator(); i.hasNext(); ) {
			   CommandLog log = i.next();
				_ps.setTimestamp(1, createTimestamp(log.getDate()));
				_ps.setInt(2, log.getPilotID());
				_ps.setString(3, log.getRemoteAddr());
				_ps.setString(4, log.getRemoteHost());
				_ps.setString(5, log.getName());
				_ps.setString(6, log.getResult());
				_ps.setLong(7, log.getTime());
				_ps.setLong(8, log.getBackEndTime());
				_ps.setBoolean(9, log.getSuccess());
				_ps.setTimestamp(10, createTimestamp(log.getDate()));
				_ps.addBatch();
			}
			
			// Do the batch update and close
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs the execution time of a Scheduled Task.
	 * @param name the Scheduled Task name
	 * @param execTime the execution time in milliseconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logTaskExecution(String name, long execTime) throws DAOException {
	   try {
	      prepareStatement("REPLACE INTO SYS_TASKS (ID, LASTRUN, RUNTIME) VALUES (?, NOW(), ?)");
	      _ps.setString(1, name);
	      _ps.setLong(2, execTime);
	      executeUpdate(1);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Purges entries out of a System log table.
	 * @param tableName the table name
	 * @param colName the date column name
	 * @param days the number of days back to set the cutoff date
	 * @return the number of entries deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(String tableName, String colName, int days) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM SYS_");
		sqlBuf.append(tableName.toUpperCase());
		sqlBuf.append(" WHERE (");
		sqlBuf.append(colName.toUpperCase());
		sqlBuf.append(" < DATE_SUB(NOW(), INTERVAL ? DAY))");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, days);
			int rowsDeleted = _ps.executeUpdate();
			_ps.close();
			return rowsDeleted;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs user authentication.
	 * @param dbName the database name
	 * @param id the User's database ID
	 * @param addr the remote IP address
	 * @param host the remote host name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void login(String dbName, int id, String addr, String host) throws DAOException {
		String h = (host == null) ? addr : host;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".SYS_LOGINS (ID, REMOTE_ADDR, REMOTE_HOST, LOGINS) VALUES (?, INET6_ATON(?), ?, 1) ON DUPLICATE KEY UPDATE LOGINS=LOGINS+1, REMOTE_HOST=?");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);
			_ps.setString(2, addr);
			_ps.setString(3, host);
			_ps.setString(4, h);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Time Zone entry to the database.
	 * @param tz the Time Zone bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(TZInfo tz) throws DAOException {
		ZoneRules zr = tz.getZone().getRules(); Instant now = Instant.now();
		try {
			prepareStatement("INSERT INTO common.TZ (CODE, NAME, ABBR, GMT_OFFSET, DST) VALUES (?, ?, ?, ?, ?)");
			_ps.setString(1, tz.getID());
			_ps.setString(2, tz.getName());
			_ps.setString(3, tz.getAbbr());
			_ps.setInt(4, zr.getStandardOffset(now).getTotalSeconds());
			_ps.setBoolean(5, (zr.nextTransition(now) != null));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing Time Zone entry in the database.
	 * @param oldID the old Time Zone code
	 * @param tz the Time Zone bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(String oldID, TZInfo tz) throws DAOException {
		ZoneRules zr = tz.getZone().getRules(); Instant now = Instant.now();
		try {
			prepareStatement("UPDATE common.TZ SET CODE=?, NAME=?, ABBR=?, GMT_OFFSET=?, DST=? WHERE (CODE=?)");
			_ps.setString(1, tz.getID());
			_ps.setString(2, tz.getName());
			_ps.setString(3, tz.getAbbr());
			_ps.setInt(4, zr.getStandardOffset(now).getTotalSeconds());
			_ps.setBoolean(5, (zr.nextTransition(now) != null));
			_ps.setString(6, oldID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
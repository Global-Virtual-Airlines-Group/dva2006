// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.servlet.CommandLog;
import org.deltava.beans.system.RegistrationBlock;

/**
 * A Data Access Object to write system logging (user commands, tasks) entries.
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
	 * Logs Web Site Command invocation.
	 * @param entries the Command log entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logCommands(Collection entries) throws DAOException {
		try {
			prepareStatement("INSERT INTO SYS_COMMANDS (CMDDATE, PILOT_ID, REMOTE_ADDR, REMOTE_HOST, "
					+ "NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS) VALUES (?, ?, INET_ATON(?), ?, ?, ?, ?, ?, ?) ON "
					+ "DUPLICATE KEY UPDATE CMDDATE=?");
			
			// Write the log entries
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
			   CommandLog log = (CommandLog) i.next();
				_ps.setTimestamp(1, createTimestamp(log.getDate()));
				_ps.setInt(2, log.getPilotID());
				_ps.setString(3, log.getRemoteAddr());
				_ps.setString(4, log.getRemoteHost());
				_ps.setString(5, log.getName());
				_ps.setString(6, log.getResult());
				_ps.setInt(7, log.getTime());
				_ps.setInt(8, log.getBackEndTime());
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
	 * Updates a Registration block entry in the database.
	 * @param block the Registration block bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(RegistrationBlock block) throws DAOException {
		try {
			prepareStatement("REPLACE INTO REG_BLOCKS (ID, FIRSTNAME, LASTNAME, REMOTE_ADDR, NETMASK, HOSTNAME, "
					+ "HAS_FEEDBACK, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, block.getID());
			_ps.setString(2, block.getFirstName());
			_ps.setString(3, block.getLastName());
			_ps.setInt(4, block.getAddress());
			_ps.setInt(5, block.getNetMask());
			_ps.setString(6, block.getHostName());
			_ps.setBoolean(7, block.getHasUserFeedback());
			_ps.setBoolean(8, block.getActive());
			executeUpdate(1);
			
			// Get new ID
			if (block.getID() == 0)
				block.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Registration block entry from the database.
	 * @param id the block ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteBlock(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM REG_BLOCKS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
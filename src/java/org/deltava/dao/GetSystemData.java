// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.stats.*;
import org.deltava.beans.system.*;
import org.deltava.taskman.TaskLastRun;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object for loading system data (Session/Command/HTTP) log tables.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSystemData extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSystemData(Connection c) {
		super(c);
	}

	/**
	 * Returns HTTP server statistics.
	 * @param orderBy the column to sort by
	 * @return a List of HTTPStatistics objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getHTTPStats(String orderBy) throws DAOException {
		try {
			prepareStatement("SELECT * FROM SYS_HTTPLOG ORDER BY " + orderBy);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List results = new ArrayList();
			while (rs.next()) {
				HTTPStatistics stats = new HTTPStatistics(expandDate(rs.getDate(1)));
				stats.setRequests(rs.getInt(2));
				stats.setHomePageHits(rs.getInt(3));
				stats.setExecutionTime(rs.getInt(4));
				stats.setBandwidth(rs.getLong(5));

				// Add to results
				results.add(stats);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the HTTP server totals.
	 * @return an HTTPTotals bean with total requests, home hits and bandwidth used
	 * @throws DAOException if a JDBC error occurs
	 * @see HTTPTotals
	 */
	public HTTPTotals getHTTPTotals() throws DAOException {
		try {
			prepareStatement("SELECT SUM(REQUESTS), SUM(HOMEHITS), SUM(BANDWIDTH) FROM SYS_HTTPLOG");
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			rs.next();
			
			// Create the result bean
			HTTPTotals totals = new HTTPTotals(rs.getInt(1), rs.getInt(2), rs.getLong(3));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return totals;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Command invocation statistics for a particular date.
	 * @param d the Date to query
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getCommands(java.util.Date d) throws DAOException {
		try {
			prepareStatement("SELECT CMDDATE, PILOT_ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, "
					+ "TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (DATE(CMDDATE) = DATE(?)) "
					+ "ORDER BY CMDDATE");
			_ps.setTimestamp(1, createTimestamp(d));
			return executeCommandLog();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns invocation statistics for a particular Web Site Command.
	 * @param cmdName the Command to query
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getCommands(String cmdName) throws DAOException {
		try {
			prepareStatement("SELECT CMDDATE, PILOT_ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, "
					+ "TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (UPPER(NAME)=?)");
			_ps.setString(1, cmdName.toUpperCase());
			return executeCommandLog();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns invocation statistics for a date and remote user address.
	 * @param d the Date to query
	 * @param remoteAddr the user's IP address
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getCommands(java.util.Date d, String remoteAddr) throws DAOException {
		try {
			prepareStatement("SELECT CMDDATE, PILOT_ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, "
					+ "TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (DATE(CMDDATE) = DATE(?)) "
					+ "AND (UCASE(REMOTE_ADDR)=?) ORDER BY CMDDATE");
			_ps.setTimestamp(1, createTimestamp(d));
			_ps.setString(2, remoteAddr.toUpperCase());
			return executeCommandLog();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to retrieve command log entries.
	 */
	private List executeCommandLog() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List results = new ArrayList();
		while (rs.next()) {
			CommandLog cmd = new CommandLog(rs.getTimestamp(1));
			cmd.setPilotID(rs.getInt(2));
			cmd.setRemoteAddr(rs.getString(3));
			cmd.setRemoteHost(rs.getString(4));
			cmd.setName(rs.getString(5));
			cmd.setResult(rs.getString(6));
			cmd.setTime(rs.getInt(7));
			cmd.setBackEndTime(rs.getInt(8));
			cmd.setSuccess(rs.getBoolean(9));

			// Add to results
			results.add(cmd);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Returns user session data for a particular Pilot.
	 * @param pilotID the Pilot's Database ID
	 * @return a List of UserSession objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getSessions(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, S.ID, S.PILOT_ID, S.START_TIME, S.END_TIME, "
					+ "INET_NTOA(S.REMOTE_ADDR), S.REMOTE_HOST FROM PILOTS P, SYS_SESSIONS S WHERE "
					+ "(P.ID=S.PILOT_ID) AND (P.ID=?) ORDER BY S.START_TIME");
			_ps.setInt(1, pilotID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List results = new ArrayList();
			while (rs.next()) {
				UserSession s = new UserSession(rs.getString(8));
				s.setPilotName(rs.getString(1), rs.getString(2));
				s.setPilotID(rs.getInt(3));
				s.setStartTime(rs.getTimestamp(4));
				s.setEndTime(rs.getTimestamp(5));
				s.setRemoteAddr(rs.getString(6));
				s.setRemoteHost(rs.getString(7));

				// Add to results
				results.add(s);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns user session data for a particular Date.
	 * @param d the Date
	 * @return a List of UserSession objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getSessions(java.util.Date d) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, S.ID, S.PILOT_ID, S.START_TIME, S.END_TIME, "
					+ "INET_NTOA(S.REMOTE_ADDR), S.REMOTE_HOST FROM PILOTS P, SYS_SESSIONS S WHERE "
					+ "(P.ID=S.PILOT_ID) AND (DATE(S.START_TIME) = DATE(?)) ORDER BY S.START_TIME");
			_ps.setTimestamp(1, createTimestamp(d));

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List results = new ArrayList();
			while (rs.next()) {
				UserSession s = new UserSession(rs.getString(8));
				s.setPilotName(rs.getString(1), rs.getString(2));
				s.setPilotID(rs.getInt(3));
				s.setStartTime(rs.getTimestamp(4));
				s.setEndTime(rs.getTimestamp(5));
				s.setRemoteAddr(rs.getString(6));
				s.setRemoteHost(rs.getString(7));

				// Add to results
				results.add(s);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Command invocation statistics.
	 * @param orderBy the column to order results by
	 * @return a Map of CommandStatsEntry beans, keyed by Command ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map getCommandStats(String orderBy) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT NAME, AVG(TOTAL_TIME) AS AVGT, AVG(BE_TIME) AS BE, "
	         + "MAX(TOTAL_TIME) AS MAXTOTAL, MAX(BE_TIME) AS MAXBE, SUM(SUCCESS) AS SC, "
	         + "COUNT(SUCCESS) AS TC FROM SYS_COMMANDS GROUP BY NAME ORDER BY ");
	   sqlBuf.append(orderBy);
	   
	   List results = null;
	   try {
	      prepareStatement(sqlBuf.toString());
	      
	      // Execute the query
	      ResultSet rs = _ps.executeQuery();

	      // Iterate through the results
	      results = new ArrayList();
	      while (rs.next()) {
	         CommandStatsEntry stat = new CommandStatsEntry(rs.getString(1));
	         stat.setAvgTime(rs.getInt(2));
	         stat.setAvgBackEndTime(rs.getInt(3));
	         stat.setMaxTime(rs.getInt(4));
	         stat.setMaxBackEndTime(rs.getInt(5));
	         stat.setSuccessCount(rs.getInt(6));
	         stat.setCount(rs.getInt(7));
	         
	         // Add to results
	         results.add(stat);
	      }
	      
	      // Clean up after ourselves
	      rs.close();
	      _ps.close();
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	   
	   // Convert to a map
	   return CollectionUtils.createMap(results, "name");
	}
	
	/**
	 * Returns the last execution date/times for Scheduled Tasks.
	 * @return a Map of TaskLastRun beans, ordered by task ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map getTaskExecution() throws DAOException {
	   
	   List results = null;
	   try {
	      prepareStatement("SELECT * FROM SYS_TASKS");
	      
	      // Execute the query
	      ResultSet rs = _ps.executeQuery();
	      
	      // Iterate through the results
	      results = new ArrayList();
	      while (rs.next())
	         results.add(new TaskLastRun(rs.getString(1), rs.getTimestamp(2)));

	      // Clean up after ourselves
	      rs.close();
	      _ps.close();
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	   
	   // Return as a map
	   return CollectionUtils.createMap(results, "name");
	}
}
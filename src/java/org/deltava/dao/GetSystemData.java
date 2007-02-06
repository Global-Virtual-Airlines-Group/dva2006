// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.servlet.CommandLog;
import org.deltava.beans.stats.*;
import org.deltava.taskman.TaskLastRun;

import org.deltava.util.cache.*;
import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object for loading system data (Session/Command/HTTP) log tables.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSystemData extends DAO {
   
   private static final Cache<HTTPTotals> _cache = new ExpiringCache<HTTPTotals>(1, 7200);

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
	public List<HTTPStatistics> getHTTPStats(String orderBy) throws DAOException {
		try {
			prepareStatement("SELECT * FROM SYS_HTTPLOG ORDER BY " + orderBy);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List<HTTPStatistics> results = new ArrayList<HTTPStatistics>();
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
	   
	   // Check the cache first
	   HTTPTotals totals = _cache.get(HTTPTotals.class);
	   if (totals != null)
	      return totals;
	   
		try {
			prepareStatement("SELECT SUM(REQUESTS), SUM(HOMEHITS), SUM(BANDWIDTH) FROM SYS_HTTPLOG");
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			rs.next();
			
			// Create the result bean
			totals = new HTTPTotals(rs.getInt(1), rs.getInt(2), rs.getLong(3));
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Add to the cache and return
			_cache.add(totals);
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
	public List<CommandLog> getCommands(java.util.Date d) throws DAOException {
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
	 * Returns invocation statistics for a remote address.
	 * @param remoteAddr the user's IP address or host name
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CommandLog> getCommands(String remoteAddr) throws DAOException {
		try {
			prepareStatement("SELECT CMDDATE, PILOT_ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, "
					+ "TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (UCASE(REMOTE_ADDR)=?) OR "
					+ "(INET_ATON(?)=REMOTE_ADDR) ORDER BY CMDDATE DESC");
			_ps.setString(1, remoteAddr.toUpperCase());
			_ps.setString(2, remoteAddr.toUpperCase());
			return executeCommandLog();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns invocation statistics for a particular user.
	 * @param pilotID the user's database ID
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CommandLog> getCommands(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT CMDDATE, PILOT_ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, "
					+ "TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (PILOT_ID=?) ORDER BY CMDDATE DESC");
			_ps.setInt(1, pilotID);
			return executeCommandLog();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to retrieve command log entries.
	 */
	private List<CommandLog> executeCommandLog() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<CommandLog> results = new ArrayList<CommandLog>();
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
	 * Returns Command invocation statistics.
	 * @param orderBy the column to order results by
	 * @return a Collection of CommandStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CommandStatsEntry> getCommandStats(String orderBy) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("SELECT NAME, AVG(TOTAL_TIME) AS AVGT, AVG(BE_TIME) AS BE, "
	         + "MAX(TOTAL_TIME) AS MAXTOTAL, MAX(BE_TIME) AS MAXBE, SUM(SUCCESS) AS SC, "
	         + "COUNT(SUCCESS) AS TC FROM SYS_COMMANDS GROUP BY NAME ORDER BY ");
	   sqlBuf.append(orderBy);
	   
	   List<CommandStatsEntry> results = new ArrayList<CommandStatsEntry>();
	   try {
	      prepareStatement(sqlBuf.toString());
	      
	      // Execute the query
	      ResultSet rs = _ps.executeQuery();
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
	      return results;
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Returns the last execution date/times for Scheduled Tasks.
	 * @return a Map of TaskLastRun beans, ordered by task ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, TaskLastRun> getTaskExecution() throws DAOException {
	   
	   List<TaskLastRun> results = new ArrayList<TaskLastRun>();
	   try {
	      prepareStatement("SELECT * FROM SYS_TASKS");
	      
	      // Execute the query
	      ResultSet rs = _ps.executeQuery();
	      
	      // Iterate through the results
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
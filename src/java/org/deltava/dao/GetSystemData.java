// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2013, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.servlet.CommandLog;
import org.deltava.beans.stats.*;
import org.deltava.taskman.TaskLastRun;

import org.deltava.util.cache.*;
import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object for loading system data (Session/Command/HTTP log tables) and Registration blocks.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetSystemData extends DAO {

	private static final Cache<HTTPTotals> _cache = CacheManager.get(HTTPTotals.class, "HTTPTotals");

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
		try (PreparedStatement ps = prepare("SELECT * FROM SYS_HTTPLOG ORDER BY " + orderBy)) {
			List<HTTPStatistics> results = new ArrayList<HTTPStatistics>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					HTTPStatistics stats = new HTTPStatistics(expandDate(rs.getDate(1)));
					stats.setRequests(rs.getInt(2));
					stats.setHomePageHits(rs.getInt(3));
					stats.setExecutionTime(rs.getLong(4));
					stats.setBandwidth(rs.getLong(5));
					results.add(stats);
				}
			}

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

		try (PreparedStatement ps = prepare("SELECT SUM(REQUESTS), SUM(HOMEHITS), SUM(BANDWIDTH) FROM SYS_HTTPLOG")) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					totals = new HTTPTotals(rs.getInt(1), rs.getInt(2), rs.getLong(3));
			}

			_cache.add(totals);
			return totals;
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
		try (PreparedStatement ps = prepare("SELECT CMDDATE, PILOT_ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS "
			+ "WHERE (UCASE(REMOTE_ADDR)=?) OR (INET6_ATON(?)=REMOTE_ADDR) ORDER BY CMDDATE DESC")) {
			ps.setString(1, remoteAddr.toUpperCase());
			ps.setString(2, remoteAddr.toUpperCase());
			return executeCommandLog(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns invocation statistics for particular user(s).
	 * @param pilotIDs a Collection of Database IDs
	 * @return a List of CommandLog objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CommandLog> getCommands(Collection<Integer> pilotIDs) throws DAOException {
		if (pilotIDs.isEmpty())
			return Collections.emptyList();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT CMDDATE, PILOT_ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, "
				+ "NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (");
		for (Iterator<Integer> i = pilotIDs.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append("(PILOT_ID=");
			sqlBuf.append(id.toString());
			sqlBuf.append(')');
			if (i.hasNext())
				sqlBuf.append(" OR ");
		}

		sqlBuf.append(") ORDER BY CMDDATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return executeCommandLog(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to retrieve command log entries.
	 */
	private static List<CommandLog> executeCommandLog(PreparedStatement ps) throws SQLException {
		List<CommandLog> results = new ArrayList<CommandLog>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				CommandLog cmd = new CommandLog(rs.getTimestamp(1).toInstant());
				cmd.setPilotID(rs.getInt(2));
				cmd.setRemoteAddr(rs.getString(3));
				cmd.setRemoteHost(rs.getString(4));
				cmd.setName(rs.getString(5));
				cmd.setResult(rs.getString(6));
				cmd.setTime(rs.getInt(7));
				cmd.setBackEndTime(rs.getInt(8));
				cmd.setSuccess(rs.getBoolean(9));
				results.add(cmd);
			}
		}

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
		StringBuilder sqlBuf = new StringBuilder("SELECT NAME, AVG(TOTAL_TIME) AS AVGT, AVG(BE_TIME) AS BE, MAX(TOTAL_TIME) AS MAXTOTAL, MAX(BE_TIME) AS MAXBE, SUM(SUCCESS) AS SC, "
			+ "COUNT(SUCCESS) AS TC FROM SYS_COMMANDS GROUP BY NAME ORDER BY ");
		sqlBuf.append(orderBy);

		List<CommandStatsEntry> results = new ArrayList<CommandStatsEntry>();
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CommandStatsEntry stat = new CommandStatsEntry(rs.getString(1));
					stat.setAvgTime(rs.getInt(2));
					stat.setAvgBackEndTime(rs.getInt(3));
					stat.setMaxTime(rs.getInt(4));
					stat.setMaxBackEndTime(rs.getInt(5));
					stat.setSuccessCount(rs.getInt(6));
					stat.setCount(rs.getInt(7));
					results.add(stat);
				}
			}

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
		Collection<TaskLastRun> results = new ArrayList<TaskLastRun>();
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM SYS_TASKS")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(new TaskLastRun(rs.getString(1), toInstant(rs.getTimestamp(2)), rs.getLong(3)));
			}

			return CollectionUtils.createMap(results, TaskLastRun::getName);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the last execution time of a Scheduled Task.
	 * @param taskID the Task ID
	 * @return the last execution date/time, or null if never
	 * @throws DAOException if a JDBC error occurs
	 */
	public java.time.Instant getLastRun(String taskID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT LASTRUN FROM SYS_TASKS WHERE (ID=?) LIMIT 1")) {
			ps.setString(1, taskID);
			try (ResultSet rs = ps.executeQuery()) { 
				return rs.next() ? toInstant(rs.getTimestamp(1)) : null;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
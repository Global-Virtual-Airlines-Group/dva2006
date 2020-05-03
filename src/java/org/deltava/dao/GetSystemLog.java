// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2013, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.servlet.CommandLog;
import org.deltava.beans.stats.APIUsage;
import org.deltava.beans.system.API;

/**
 * A Data Access Object to read system logging tables. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetSystemLog extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSystemLog(Connection c) {
		super(c);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT CMDDATE, PILOT_ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, NAME, RESULT, TOTAL_TIME, BE_TIME, SUCCESS FROM SYS_COMMANDS WHERE (");
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
	
	/**
	 * Retrtieves external API request statistics from the database.
	 * @param api the API, or null for all
	 * @return a List of APIUsage beans 
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<APIUsage> getAPIRequests(API api) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT USAGE_DATE, API, SUM(USE_COUNT), SUM(ANONYMOUS) FROM SYS_API_USAGE ");
		if (api != null)
			sqlBuf.append("WHERE (LEFT(API,?)=?) ");
		sqlBuf.append("GROUP BY DATE(USAGE_DATE) ORDER BY USAGE_DATE DESC, API");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (api != null) {
				ps.setInt(1, api.name().length() + 1);
				ps.setString(2, api.createName(""));
			}
			
			// Execute the Query
			List<APIUsage> results = new ArrayList<APIUsage>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					APIUsage ap = new APIUsage(toInstant(rs.getTimestamp(1)), rs.getString(2));
					ap.setTotal(rs.getInt(3));
					ap.setAnonymous(rs.getInt(4));
					results.add(ap);
				}
			}
			
			return results;
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
}
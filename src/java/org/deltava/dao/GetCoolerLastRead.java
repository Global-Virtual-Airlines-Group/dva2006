// Copyright 2014, 2015, 2016, 2019, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.util.StringUtils;

import java.time.Instant;

/**
 * A Data Access Object to read Water Cooler last read marks.
 * @author Luke
 * @version 11.3
 * @since 5.4
 */

public class GetCoolerLastRead extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use 
	 */
	public GetCoolerLastRead(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the last date/time a user read a particular Cooler message thread.
	 * @param threadID the Message Thread ID
	 * @param userID the user ID
	 * @return the last read date/time or null if never
	 * @throws DAOException if a JDBC error occurs
	 */
	public Instant getLastRead(int threadID, int userID) throws DAOException {
		Integer ID = Integer.valueOf(threadID);
		Map<Integer, Instant> results = getLastRead(Collections.singleton(ID), userID);
		return results.get(ID);
	}
	
	/**
	 * Returns the date/time all users last read a particular Cooler message thread.
	 * @param threadID the Message Thread ID
	 * @return a Map of last read date/times, keyed by user ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Instant> getLastRead(int threadID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AUTHOR_ID, LASTREAD FROM common.COOLER_LASTREAD WHERE (ID=?)")) {
			ps.setInt(1, threadID);
			Map<Integer, Instant> results = new HashMap<Integer, Instant>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), rs.getTimestamp(2).toInstant());
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the last date/time a user read several Cooler message threads.
	 * @param ids the thread IDs or Message Threads
	 * @param userID the user ID
	 * @return a Map of last read date/times, keyed by message thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, java.time.Instant> getLastRead(Collection<?> ids, int userID) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, LASTREAD FROM common.COOLER_LASTREAD WHERE (AUTHOR_ID=?) AND (ID IN (");
		sqlBuf.append(StringUtils.listConcat(toID(ids), ","));
		sqlBuf.append("))");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, userID);
			Map<Integer, Instant> results = new HashMap<Integer, Instant>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), rs.getTimestamp(2).toInstant());
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
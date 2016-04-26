// Copyright 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;

/**
 * A Data Access Object to read Water Cooler last read marks.
 * @author Luke
 * @version 7.0
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
	public java.time.Instant getLastRead(int threadID, int userID) throws DAOException {
		Integer ID = Integer.valueOf(threadID);
		Map<Integer, java.time.Instant> results = getLastRead(Collections.singleton(ID), userID);
		return results.get(ID);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, LASTREAD FROM common.COOLER_LASTREAD WHERE "
			+ "(AUTHOR_ID=?) AND (ID IN (");
		for (Iterator<?> i = ids.iterator(); i.hasNext(); ) {
			Object rawID = i.next();
			Integer id = (rawID instanceof Integer) ? (Integer) rawID : Integer.valueOf(((DatabaseBean) rawID).getID());
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append("))");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, userID);
			Map<Integer, java.time.Instant> results = new HashMap<Integer, java.time.Instant>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), rs.getTimestamp(2).toInstant());
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
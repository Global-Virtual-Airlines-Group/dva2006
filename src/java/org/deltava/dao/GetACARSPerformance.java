// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.TaskTimerData;

/**
 * A Data Access Object to load ACARS client performance counter data from the database.
 * @author Luke
 * @version 9.0
 * @since 8.6
 */

public class GetACARSPerformance extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSPerformance(Connection c) {
		super(c);
	}

	/**
	 * Retrieves timer data for a given flight.
	 * @param flightID the ACARS Flight ID
	 * @return a Collection of TaskTimerData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TaskTimerData> getTimers(int flightID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.PERFINFO WHERE (ID=?)")) {
			ps.setInt(1, flightID);
			
			Collection<TaskTimerData> results = new ArrayList<TaskTimerData>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					TaskTimerData ttd = new TaskTimerData(rs.getString(2), rs.getInt(3));
					ttd.setCount(rs.getLong(4));
					ttd.setTotal(rs.getLong(5));
					ttd.setMin(rs.getInt(6));
					ttd.setMax(rs.getInt(7));
					results.add(ttd);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
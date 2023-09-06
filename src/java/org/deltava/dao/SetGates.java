// Copyright 2015, 2017, 2018, 2019, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.navdata.Gate;
import org.deltava.beans.schedule.Airline;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write Gate data. 
 * @author Luke
 * @version 11.1
 * @since 6.3
 */

public class SetGates extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetGates(Connection c) {
		super(c);
	}

	/**
	 * Updates airline Gate usage data. 
	 * @param gates a Collection of Gate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Collection<Gate> gates) throws DAOException {
		Collection<String> cacheKeys = gates.stream().map(g -> "AP-" + g.getCode()).collect(Collectors.toSet());
		try {
			startTransaction();
			
			// Clear gates
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.GATE_AIRLINES WHERE (ICAO=?) AND (NAME=?)")) {
				for (Gate g : gates) {
					ps.setString(1, g.getCode());
					ps.setString(2, g.getName());
					ps.addBatch();
				}
			
				executeUpdate(ps, 0, 0);
			}
			
			// Write gate data
			int totalWrites = gates.stream().filter(g -> !g.getAirlines().isEmpty()).mapToInt(g -> g.getAirlines().size()).sum();
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.GATE_AIRLINES (ICAO, NAME, AIRLINE, ZONE) VALUES (?, ?, ?, ?)")) {
				for (Gate g : gates) {
					ps.setString(1, g.getCode());
					ps.setString(2, g.getName());
					ps.setInt(4, g.getZone().ordinal());
					for (Airline a : g.getAirlines()) {
						ps.setString(3, a.getCode());
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, totalWrites);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			cacheKeys.forEach(k -> CacheManager.invalidate("Gates", k));
		}
	}
}
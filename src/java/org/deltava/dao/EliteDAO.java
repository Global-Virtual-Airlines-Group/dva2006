// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.LocalDate;

import org.deltava.beans.econ.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read Elite program definitions. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

abstract class EliteDAO extends DAO {
	
	/**
	 * The EliteLevel cache.
	 */
	protected static final Cache<EliteLevel> _lvlCache = CacheManager.get(EliteLevel.class, "EliteLevel");
	
	/**
	 * The EliteStatus cache.
	 */
	protected static final Cache<EliteStatus> _stCache = CacheManager.get(EliteStatus.class, "EliteStatus");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected EliteDAO(Connection c) {
		super(c);
	}
	
	/**
	 * Returns an Elite status level for the current year.
	 * @param name the level name
	 * @return an EliteLevel, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EliteLevel get(String name) throws DAOException {
		return get(name, LocalDate.now().getYear(), SystemData.get("airline.db"));
	}

	/**
	 * Returns an Elite status level for a particular year.
	 * @param name the level name
	 * @param year the year
	 * @param dbName the database name
	 * @return an EliteLevel, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EliteLevel get(String name, int year, String dbName) throws DAOException {
		
		// Check the cache
		EliteLevel lvl = _lvlCache.get(new EliteLevel(year, name).cacheKey());
		if (lvl != null)
			return lvl;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".ELITE_LEVELS WHERE (NAME=?) AND (YR=?) LIMIT 1");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, name);
			ps.setInt(2, year);
			return executeLevel(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse elite level result sets.
	 * @param ps the PreparedStatement to execute
	 * @return a List of EliteLevel beans
	 * @throws SQLException if a JDBC error occurs
	 */
	protected static List<EliteLevel> executeLevel(PreparedStatement ps) throws SQLException {
		List<EliteLevel> results = new ArrayList<EliteLevel>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				EliteLevel lvl = new EliteLevel(rs.getInt(2), rs.getString(1));
				lvl.setLegs(rs.getInt(3));
				lvl.setDistance(rs.getInt(4));
				lvl.setPoints(rs.getInt(5));
				lvl.setBonusFactor(rs.getInt(6) / 100.0f);
				lvl.setColor(rs.getInt(7));
				lvl.setTargetPercentile(rs.getInt(8));
				lvl.setVisible(rs.getBoolean(9));
				results.add(lvl);
				_lvlCache.add(lvl);
			}
		}
		
		return results;
	}

	/**
	 * Helper method to populate EliteLevel beans.
	 * @param data a Collection of EliteLeveBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	protected void populateLevels(Collection<? extends EliteLevelBean> data) throws DAOException {
		for (EliteLevelBean eb : data) {
			EliteLevel lvl = get(eb.getLevel().getName(), eb.getLevel().getYear(), SystemData.get("airline.db"));
			if (lvl != null)
				eb.setLevel(lvl);
		}
	}
}
// Copyright 2020, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.econ.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Elite program definitions. 
 * @author Luke
 * @version 11.5
 * @since 9.2
 */

abstract class EliteDAO extends DAO {
	
	/**
	 * The EliteLevel cache.
	 */
	protected static final Cache<EliteLevel> _lvlCache = CacheManager.get(EliteLevel.class, "EliteLevel");
	
	/**
	 * The Elite status cache.
	 */
	protected static final Cache<EliteStatus> _stCache = CacheManager.get(EliteStatus.class, "EliteStatus");
	
	/**
	 * The Elite lifetime status cache.
	 */
	protected static final Cache<EliteLifetimeStatus> _lstCache = CacheManager.get(EliteLifetimeStatus.class, "EliteLTStatus");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected EliteDAO(Connection c) {
		super(c);
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
		String db = formatDBName(dbName);
		EliteLevel lvl = _lvlCache.get(new EliteLevel(year, name, db).cacheKey());
		if (lvl != null)
			return lvl;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EL.*, DATABASE() FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".ELITE_LEVELS EL WHERE (NAME=?) AND (YR=?) LIMIT 1");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, name);
			ps.setInt(2, year);
			return executeLevel(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns a lifetime Elite status level.
	 * @param name the level name
	 * @param dbName the database name
	 * @return an EliteLifetime, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EliteLifetime getLifetime(String name, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EL.*, DATABASE() FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".ELITE_LIFETIME EL WHERE (EL.NAME=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, name);
			List<EliteLifetime> results = executeLifetime(ps);
			populateLevels(results);
			return results.isEmpty() ? null : results.getFirst();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Elite level result sets.
	 * @param ps the PreparedStatement to execute
	 * @return a List of EliteLevel beans
	 * @throws SQLException if a JDBC error occurs
	 */
	protected static List<EliteLevel> executeLevel(PreparedStatement ps) throws SQLException {
		List<EliteLevel> results = new ArrayList<EliteLevel>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				EliteLevel lvl = new EliteLevel(rs.getInt(2), rs.getString(1), rs.getString(11));
				lvl.setStatisticsStartDate(expandDate(rs.getDate(3)));
				lvl.setLegs(rs.getInt(4));
				lvl.setDistance(rs.getInt(5));
				lvl.setPoints(rs.getInt(6));
				lvl.setBonusFactor(rs.getInt(7) / 100.0f);
				lvl.setColor(rs.getInt(8));
				lvl.setTargetPercentile(rs.getInt(9));
				lvl.setVisible(rs.getBoolean(10));
				results.add(lvl);
				_lvlCache.add(lvl);
			}
		}
		
		return results;
	}
	
	/**
	 * Helper method to process Elite lifetime level result sets.
	 * @param ps the PreparedStatement to execute
	 * @return a List of EliteLifetime beans
	 * @throws SQLException if a JDBC error occurs
	 */
	protected static List<EliteLifetime> executeLifetime(PreparedStatement ps) throws SQLException {
		List<EliteLifetime> results = new ArrayList<EliteLifetime>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				EliteLifetime el = new EliteLifetime(rs.getString(1));
				el.setCode(rs.getString(2));
				el.setDistance(rs.getInt(3));
				el.setLegs(rs.getInt(4));
				el.setLevel(new EliteLevel(rs.getInt(6), rs.getString(5), rs.getString(7)));
				results.add(el);
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
			EliteLevel lvl = get(eb.getLevel().getName(), eb.getLevel().getYear(), eb.getLevel().getOwner());
			if (lvl != null)
				eb.setLevel(lvl);
		}
	}
}
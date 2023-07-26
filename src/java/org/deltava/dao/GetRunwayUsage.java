// Copyright 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RunwayUsage;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load popular runways for takeoff and landing.
 * @author Luke
 * @version 11.1
 * @since 2.6
 */

public class GetRunwayUsage extends DAO {
	
	private static final Cache<RunwayUsage> _cache = CacheManager.get(RunwayUsage.class, "RunwayUsage");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRunwayUsage(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves the most popular runways used at a particular Airport.
	 * @param a the Airport
	 * @param isDeparture TRUE if takeoff, otherwise landing
	 * @return a RunwayUsage bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public RunwayUsage getPopularRunways(Airport a, boolean isDeparture) throws DAOException {
		
		// Check the cache
		RunwayUsage rgu = new RunwayUsage(RoutePair.of(a, null), isDeparture);
		RunwayUsage rwyUse = _cache.get(rgu.cacheKey());
		if (rwyUse != null)
			return rwyUse.clone();

		try (PreparedStatement ps = prepareWithoutLimits("SELECT R.RUNWAY, COUNT(R.ID) AS CNT FROM acars.RWYDATA R WHERE (R.ICAO=?) AND (R.ISTAKEOFF=?) GROUP BY R.RUNWAY")) {
			ps.setString(1, a.getICAO());
			ps.setBoolean(2, isDeparture);
			
			// Execute the Query
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					rgu.add(rs.getString(1), rs.getInt(2));
			}
			
			_cache.add(rgu);
			return rgu.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the most popular runways used on a particular route.
	 * @param rp the RoutePair
	 * @param isDeparture TRUE if takeoff, otherwise landing
	 * @return a RunwayUsage bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public RunwayUsage getPopularRunways(RoutePair rp, boolean isDeparture) throws DAOException {
		
		// Check the cache
		RunwayUsage rgu = new RunwayUsage(rp, isDeparture);
		RunwayUsage rwyUse = _cache.get(rgu.cacheKey());
		if (rwyUse != null)
			return rwyUse.clone();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT R.RUNWAY, COUNT(R.ID) AS CNT FROM acars.RWYDATA R, acars.FLIGHTS F WHERE (R.ID=F.ID) AND (R.ISTAKEOFF=?) "); 
		if (rp.getAirportD() != null)
			sqlBuf.append("AND (F.AIRPORT_D=?) ");
		if (rp.getAirportA() != null)
			sqlBuf.append("AND (F.AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY R.RUNWAY ORDER BY CNT DESC");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int pos = 1;
			ps.setBoolean(1, isDeparture);
			if (rp.getAirportD() != null)
				ps.setString(++pos, rp.getAirportD().getIATA());
			if (rp.getAirportA() != null)
				ps.setString(++pos, rp.getAirportA().getIATA());
			
			// Execute the Query
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					rgu.add(rs.getString(1), rs.getInt(2));
			}
			
			_cache.add(rgu);
			return rgu.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
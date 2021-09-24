// Copyright 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load popular runways for takeoff and landing.
 * @author Luke
 * @version 10.2
 * @since 2.6
 */

public class GetACARSRunways extends DAO {
	
	private static final Cache<CacheableList<RunwayUsage>> _cache = CacheManager.getCollection(RunwayUsage.class, "ACARSRunways");

	private static class RunwayCacheKey implements java.io.Serializable {
		private final String _key;
		
		RunwayCacheKey(Airport aD, Airport aA, boolean isTakeoff) {
			super();
			String aDCode = (aD == null) ? "null" : aD.getICAO();
			String aACode = (aA == null) ? "null" : aA.getICAO();
			_key = aDCode + "$" + aACode + "$" + String.valueOf(isTakeoff);
		}
		
		@Override
		public int hashCode() {
			return _key.hashCode();
		}
		
		@Override
		public String toString() {
			return _key;
		}
		
		@Override
		public boolean equals(Object o) {
			return _key.equals(String.valueOf(o));
		}
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSRunways(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves the most popular runways used at a particular Airport.
	 * @param a the Airport
	 * @param isTakeoff TRUE if takeoff, otherwise landing
	 * @return a List of Runway beans, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RunwayUsage> getPopularRunways(Airport a, boolean isTakeoff) throws DAOException {
		
		// Build the cache key
		RunwayCacheKey key = new RunwayCacheKey(isTakeoff ? a : null, isTakeoff ? null : a, isTakeoff);
		CacheableList<RunwayUsage> rwys = _cache.get(key);
		if (rwys != null)
			return rwys.clone();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ND.NAME, ND.CODE, ND.LATITUDE, ND.LONGITUDE, ND.ALTITUDE, ND.HDG, IFNULL(RW.WIDTH, 150), IFNULL(ND.FREQ,'-'), RR.OLDCODE, RW.THRESHOLD, "
			+ "COUNT(R.ID) AS CNT FROM acars.RWYDATA R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND ((R.RUNWAY=RR.NEWCODE) OR (R.RUNWAY=RR.NEWCODE))) LEFT JOIN "
			+ "common.NAVDATA ND ON ((ND.CODE=R.ICAO) AND (ND.NAME=IFNULL(RR.NEWCODE,R.RUNWAY)) AND (ND.ITEMTYPE=?)) LEFT JOIN common.RUNWAYS RW ON ((ND.CODE=RW.ICAO) AND "
			+ "(ND.NAME=RW.NAME) AND (RW.SIMVERSION=?)) WHERE (R.ICAO=?) AND (R.ISTAKEOFF=?) AND (ND.NAME IS NOT NULL) GROUP BY ND.NAME ORDER BY CNT DESC");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, Navaid.RUNWAY.ordinal());
			ps.setInt(2, Simulator.P3Dv4.getCode());
			ps.setString(3, a.getICAO());
			ps.setBoolean(4, isTakeoff);
			
			// Execute the Query
			rwys = new CacheableList<RunwayUsage>(key);
			rwys.addAll(execute(ps));
			_cache.add(rwys);
			return rwys.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the most popular runways used at a particular Aairport.
	 * @param aD the departure Airport bean
	 * @param aA the arrival Airport bean, or null if none
	 * @param isTakeoff TRUE if takeoff, otherwise landing
	 * @return a List of Runway beans, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RunwayUsage> getPopularRunways(Airport aD, Airport aA, boolean isTakeoff) throws DAOException {
		
		// Build the cache key
		RunwayCacheKey key = new RunwayCacheKey(aD, aA, isTakeoff);
		CacheableList<RunwayUsage> rwys = _cache.get(key);
		if (rwys != null)
			return rwys.clone();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ND.NAME, ND.CODE, ND.LATITUDE, ND.LONGITUDE, ND.ALTITUDE, ND.HDG, IFNULL(RW.WIDTH, 150), IFNULL(ND.FREQ,'-'), RR.OLDCODE, RW.THRESHOLD, "
			+ "COUNT(R.ID) AS CNT FROM acars.FLIGHTS F, acars.RWYDATA R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND ((R.RUNWAY=RR.OLDCODE) OR (R.RUNWAY=RR.NEWCODE))) "
			+ "LEFT JOIN common.NAVDATA ND ON ((ND.CODE=R.ICAO) AND (ND.NAME=IFNULL(RR.NEWCODE, R.RUNWAY)) AND (ND.ITEMTYPE=?)) LEFT JOIN common.RUNWAYS RW ON ((ND.CODE=RW.ICAO) AND "
			+ "(ND.NAME=RW.NAME) AND (RW.SIMVERSION=?)) WHERE (F.ID=R.ID) AND (R.ISTAKEOFF=?) AND (ND.NAME IS NOT NULL) ");
		if (aD != null)
			sqlBuf.append("AND (F.AIRPORT_D=?) ");
		if (aA != null)
			sqlBuf.append("AND (F.AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY ND.NAME ORDER BY CNT DESC");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int pos = 0;
			ps.setInt(++pos, Navaid.RUNWAY.ordinal());
			ps.setInt(++pos, Simulator.P3Dv4.getCode());
			ps.setBoolean(++pos, isTakeoff);
			if (aD != null)
				ps.setString(++pos, aD.getIATA());
			if (aA != null)
				ps.setString(++pos, aA.getIATA());
			
			// Execute the Query
			rwys = new CacheableList<RunwayUsage>(key);
			rwys.addAll(execute(ps));
			_cache.add(rwys);
			return rwys.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse popular runway result sets.
	 */
	private static Collection<RunwayUsage> execute(PreparedStatement ps) throws SQLException {
		Collection<RunwayUsage> results = new LinkedHashSet<RunwayUsage>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				RunwayUsage r = new RunwayUsage(rs.getDouble(3), rs.getDouble(4));
				r.setName(rs.getString(1));
				r.setCode(rs.getString(2));
				r.setLength(rs.getInt(5));
				r.setHeading(rs.getInt(6));
				r.setWidth(rs.getInt(7));
				r.setFrequency(rs.getString(8));
				r.setOldCode(rs.getString(9));
				r.setThresholdLength(rs.getInt(10));
				r.setUseCount(rs.getInt(11));
				results.add(r);
			}
		}
		
		return results;
	}
}
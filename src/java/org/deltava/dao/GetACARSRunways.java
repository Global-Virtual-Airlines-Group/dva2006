// Copyright 2009, 2010, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.UseCount;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load popular runways for takeoff and landing.
 * @author Luke
 * @version 7.2
 * @since 2.6
 */

public class GetACARSRunways extends DAO {
	
	private static final Cache<CacheableList<Runway>> _cache = CacheManager.getCollection(Runway.class, "ACARSRunways");

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
	
	public static class SelectableRunway extends Runway implements UseCount {
		private int _useCount;
		
		SelectableRunway(double lat, double lng) {
			super(lat, lng);
		}
		
		@Override
		public int getUseCount() {
			return _useCount;
		}
		
		public void setUseCount(int uses) {
			_useCount = uses;
		}
		
		@Override
		public int hashCode() {
			return getComboAlias().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			return (o instanceof SelectableRunway) && (hashCode() == o.hashCode());
		}
		
		@Override
		public int compareTo(NavigationDataBean nd2) {
			if (!(nd2 instanceof SelectableRunway))
				return super.compareTo(nd2);
			
			SelectableRunway sr2 = (SelectableRunway) nd2;
			int tmpResult = Integer.valueOf(_useCount).compareTo(Integer.valueOf(sr2._useCount));
			if (tmpResult == 0)
				tmpResult = getName().compareTo(sr2.getName());
			
			return (tmpResult == 0) ? getCode().compareTo(sr2.getCode()) : tmpResult;
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
	 * Retrieves the most popular runways used at a particular airport. Runways used less than 1/10th as much as the most
	 * popular runway are assumed to be erroneous and removed.
	 * @param aD the departure Airport bean
	 * @param aA the arrival Airport bean, or null if none
	 * @param isTakeoff TRUE if takeoff, otherwise landing
	 * @return a List of Runway beans, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Runway> getPopularRunways(Airport aD, Airport aA, boolean isTakeoff) throws DAOException {
		
		// Build the cache key
		RunwayCacheKey key = new RunwayCacheKey(aD, aA, isTakeoff);
		CacheableList<Runway> rwys = _cache.get(key);
		if (rwys != null)
			return rwys.clone();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT R.RUNWAY, R.ICAO, ND.LATITUDE, ND.LONGITUDE, ND.ALTITUDE, ND.HDG, "
				+ "IF(ND.FREQ=?,NULL, ND.FREQ) AS FREQ, COUNT(R.ID) AS CNT FROM acars.FLIGHTS F, acars.RWYDATA R LEFT JOIN "
				+ "common.NAVDATA ND ON (ND.CODE=R.ICAO) AND (ND.NAME=R.RUNWAY) AND (ND.ITEMTYPE=?) WHERE (F.ID=R.ID) "
				+ "AND (R.ISTAKEOFF=?) ");
		if (aD != null)
			sqlBuf.append("AND (F.AIRPORT_D=?) ");
		if (aA != null)
			sqlBuf.append("AND (F.AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY R.RUNWAY ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(++pos, "-");
			_ps.setInt(++pos, Navaid.RUNWAY.ordinal());
			_ps.setBoolean(++pos, isTakeoff);
			if (aD != null)
				_ps.setString(++pos, aD.getIATA());
			if (aA != null)
				_ps.setString(++pos, aA.getIATA());
			
			// Execute the Query
			int max = 0;
			Collection<Runway> results = new LinkedHashSet<Runway>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					SelectableRunway r = new SelectableRunway(rs.getDouble(3), rs.getDouble(4));
					r.setName(rs.getString(1));
					r.setCode(rs.getString(2));
					r.setLength(rs.getInt(5));
					r.setHeading(rs.getInt(6));
					r.setFrequency(rs.getString(7));
					r.setUseCount(rs.getInt(8));
					max = Math.max(max, r.getUseCount());
					
					// Determine percentage - default to 10%, if we have more than 10,000 flights use 20%
					int maxRatio = (max > 5000) ? 4 : 10;
					if ((r.getUseCount() > (max / maxRatio)) || (max < 20))
						results.add(r);
					else
						break;
				}
			}
				
			_ps.close();
			
			// Add to the cache and return
			rwys = new CacheableList<Runway>(key);
			rwys.addAll(results);
			_cache.add(rwys);
			return rwys.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
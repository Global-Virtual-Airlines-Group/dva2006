// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load popular runways for takeoff and landing.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class GetACARSRunways extends DAO implements CachingDAO {
	
	private static final Cache<CacheableCollection<Runway>> _cache = new AgingCache<CacheableCollection<Runway>>(128);

	private class RunwayCacheKey {
		private String _key;
		
		RunwayCacheKey(Airport aD, Airport aA, boolean isTakeoff) {
			super();
			String aACode = (aA == null) ? "null" : aA.getICAO();
			_key = aD.getICAO() + "$" + aACode + "$" + String.valueOf(isTakeoff);
		}
		
		public int hashCode() {
			return _key.hashCode();
		}
		
		public String toString() {
			return _key;
		}
		
		public boolean equals(Object o) {
			return (_key.equals(String.valueOf(o)));
		}
	}
	
	private class SelectableRunway extends Runway implements Comparable<NavigationDataBean> {
		private int _useCount;
		
		SelectableRunway(double lat, double lng) {
			super(lat, lng);
		}
		
		public int getUseCount() {
			return _useCount;
		}
		
		public void setUseCount(int uses) {
			_useCount = uses;
		}
		
		public int hashCode() {
			return (getCode() + " RW" + getName()).hashCode();
		}
		
		public boolean equals(Object o) {
			return (o instanceof SelectableRunway) && (hashCode() == o.hashCode());
		}
		
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
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
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
		Collection<Runway> rwys = _cache.get(key);
		if (rwys != null)
			return new ArrayList<Runway>(rwys);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT R.RUNWAY, R.ICAO, ND.LATITUDE, ND.LONGITUDE, ND.ALTITUDE, ND.HDG, "
				+ "IF(ND.FREQ=?,NULL, ND.FREQ) AS FREQ, COUNT(R.ID) AS CNT FROM acars.FLIGHTS F, acars.RWYDATA R LEFT JOIN "
				+ "common.NAVDATA ND ON (ND.CODE=R.ICAO) AND (ND.NAME=R.RUNWAY) AND (ND.ITEMTYPE=?) WHERE (F.ID=R.ID) "
				+ "AND (R.ISTAKEOFF=?) AND (F.AIRPORT_D=?) ");
		if (aA != null)
			sqlBuf.append(" AND (F.AIRPORT_A=?)");
		sqlBuf.append("GROUP BY R.RUNWAY ORDER BY CNT DESC");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, "-");
			_ps.setInt(2, NavigationDataBean.RUNWAY);
			_ps.setBoolean(3, isTakeoff);
			_ps.setString(4, aD.getIATA());
			if (aA != null)
				_ps.setString(5, aA.getIATA());
			
			// Execute the Query
			int max = 0;
			CacheableCollection<Runway> results = new CacheableSet<Runway>(key);
			assert (results instanceof LinkedHashSet<?>);
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				SelectableRunway r = new SelectableRunway(rs.getDouble(3), rs.getDouble(4));
				r.setName(rs.getString(1));
				r.setCode(rs.getString(2));
				r.setLength(rs.getInt(5));
				r.setHeading(rs.getInt(6));
				r.setFrequency(rs.getString(7));
				r.setUseCount(rs.getInt(8));
				max = Math.max(max, r.getUseCount());
				if (r.getUseCount() > (max / 10))
					results.add(r);
			}
				
			// Clean up
			rs.close();
			_ps.close();
			
			// Add to the cache and return
			_cache.add(results);
			return new ArrayList<Runway>(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
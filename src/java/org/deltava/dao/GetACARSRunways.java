// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load popular runways for takeoff and landing.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class GetACARSRunways extends DAO implements CachingDAO {
	
	private static final Cache<CacheableCollection<String>> _cache = new AgingCache<CacheableCollection<String>>(128);

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
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _cache.getHits();
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
	 * @return a List of runway codes, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getPopularRunways(Airport aD, Airport aA, boolean isTakeoff) throws DAOException {
		
		// Build the cache key
		RunwayCacheKey key = new RunwayCacheKey(aD, aA, isTakeoff);
		Collection<String> rwys = _cache.get(key);
		if (rwys != null)
			return new ArrayList<String>(rwys);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT R.RUNWAY, COUNT(R.ID) AS CNT from acars.RWYDATA R, acars.FLIGHTS F "
				+ "WHERE (F.ID=R.ID) AND (R.ISTAKEOFF=?) AND (F.AIRPORT_D=?) ");
		if (aA != null)
			sqlBuf.append(" AND (F.AIRPORT_A=?)");
		sqlBuf.append("GROUP BY R.RUNWAY ORDER BY CNT DESC");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setBoolean(1, isTakeoff);
			_ps.setString(2, aD.getIATA());
			if (aA != null)
				_ps.setString(3, aA.getIATA());
			
			// Execute the Query
			int max = 0;
			CacheableCollection<String> results = new CacheableSet<String>(key);
			assert (results instanceof LinkedHashSet<?>);
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				int cnt = rs.getInt(2);
				max = Math.max(max, cnt);
				if (cnt > (max / 10))
					results.add("RW" + rs.getString(1));
			}
				
			// Clean up
			rs.close();
			_ps.close();
			
			// Add to the cache and return
			_cache.add(results);
			return new ArrayList<String>(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
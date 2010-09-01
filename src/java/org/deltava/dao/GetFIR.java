// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.FIR;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load FIR data.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class GetFIR extends DAO implements CachingDAO {
	
	private static final Cache<FIR> _cache = new AgingCache<FIR>(64);
	private static final Cache<CacheableString> _idCache = new AgingCache<CacheableString>(128);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFIR(Connection c) {
		super(c);
	}

	@Override
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_cache);
		info.add(_idCache);
		return info;
	}

	/**
	 * Retrieves FIR data.
	 * @param id the FIR ID.
	 * @param isOceanic TRUE if an Oceanic FIR, otherwise FALSE
	 * @return an FIR bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FIR get(String id, boolean isOceanic) throws DAOException {
		
		// Get the ID
		CacheableString idCacheKey = _idCache.get(id);
		if ((idCacheKey != null) && (!idCacheKey.getValue().equals(id)))
			return get(idCacheKey.getValue(), isOceanic);

		// Build the cache key
		String cacheKey = id.trim().toUpperCase();
		if (isOceanic)
			cacheKey += " Oceanic";
		
		// Check the cache
		FIR fir = _cache.get(cacheKey);
		if (fir != null)
			return fir;
		
		try {
			prepareStatementWithoutLimits("SELECT F.ID, F.NAME, F.OCEANIC FROM common.FIR F LEFT JOIN common.FIRALIAS FA "
				+ "ON (F.ID=FA.ID) WHERE (F.ID=?) OR (FA.ALIAS=?) ORDER BY IF(F.OCEANIC=?, 0, 1) LIMIT 1");
			_ps.setString(1, id);
			_ps.setString(2, id);
			_ps.setBoolean(3, isOceanic);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				fir = new FIR(rs.getString(1));
				fir.setName(rs.getString(2));
				fir.setOceanic(rs.getBoolean(3));
				
				// Clean up
				rs.close();
				_ps.close();
				
				// Load border coordinates
				prepareStatementWithoutLimits("SELECT LAT, LNG FROM common.FIRDATA WHERE (ID=?) AND (OCEANIC=?) ORDER BY SEQ");
				_ps.setString(1, fir.getID());
				_ps.setBoolean(2, fir.isOceanic());				
				rs = _ps.executeQuery();
				while (rs.next())
					fir.addBorderPoint(new GeoPosition(rs.getDouble(1), rs.getDouble(2)));
				
				// Clean up
				rs.close();
				_ps.close();
				
				// Load aliases
				prepareStatementWithoutLimits("SELECT ALIAS FROM common.FIRALIAS WHERE (ID=?) AND (OCEANIC=?)");
				_ps.setString(1, fir.getID());
				_ps.setBoolean(2, fir.isOceanic());				
				rs = _ps.executeQuery();
				while (rs.next()) {
					fir.addAlias(rs.getString(1));
					_idCache.add(new CacheableString(id, rs.getString(1)));
				}
				
				// Clean up and add to cache
				rs.close();
				_ps.close();
				_cache.add(fir);
			}
			
			return fir;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
// Copyright 2010, 2011, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.Continent;
import org.deltava.beans.schedule.Country;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load ISO-3316 country codes and perform geolocation.
 * @author Luke
 * @version 8.7
 * @since 3.2
 */

public class GetCountry extends DAO {
	
	private static final GeoCache<CacheableString> _cache = CacheManager.getGeo(CacheableString.class, "GeoCountry");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCountry(Connection c) {
		super(c);
	}

	/**
	 * Initializes all country codes.
	 * @return the number of countries loaded
	 * @throws DAOException if a JDBC error occurs
	 */
	public int initAll() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT CODE, NAME, CONTINENT FROM common.COUNTRY");
			int rowsLoaded = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Country.init(rs.getString(1), rs.getString(2), Continent.valueOf(rs.getString(3)));
					rowsLoaded++;
				}
			}
			
			_ps.close();
			return rowsLoaded;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Determines what country covers a particular point.
	 * @param loc a GeoLocation
	 * @param isAirspace TRUE if we want INTL returned if no match, otherwise FALSE
	 * @return a Country object, or null/INTL if none found
	 * @throws DAOException if a JDBC error occurs
	 * @see Country#INTL
	 */
	public Country find(GeoLocation loc, boolean isAirspace) throws DAOException {
		
		// Check the cache
		CacheableString id = _cache.get(loc);
		if (id != null)
			return Country.get(id.getValue());
		
		try {
			prepareStatementWithoutLimits("SELECT CODE FROM common.COUNTRY_GEO WHERE ST_Contains(DATA, ST_PointFromText(?,?)) LIMIT 1");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, WGS84_SRID);
			
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					id = new CacheableString(rs.getString(1), rs.getString(1));
			}

			_ps.close();
			
			// Add to cache
			if (id != null) {
				_cache.add(loc, id);
				return Country.get(id.getValue());
			} else if (isAirspace) {
				_cache.add(loc, new CacheableString("", Country.INTL.getCode()));
				return Country.INTL;
			}

			return null;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}
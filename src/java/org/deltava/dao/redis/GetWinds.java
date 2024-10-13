// Copyright 2014, 2015, 2016, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

import org.deltava.util.JedisUtils;

/**
 * A Data Access Object to load winds aloft data from Redis.
 * @author Luke
 * @version 11.3
 * @since 5.4
 */

public class GetWinds extends RedisDAO {

	/**
	 * Retrieves the winds for a particular location.
	 * @param loc the GeospaceLocation
	 * @return the closest WindData data
	 * @throws DAOException if a JDBC error occurs
	 */
	public WindData getWinds(GeospaceLocation loc) throws DAOException {
		PressureLevel lvl = PressureLevel.getClosest(loc.getAltitude());
		
		// Generate the key by rounding to the nearest half-degree
		double rLat = Math.round(loc.getLatitude() / 2) * 2; double rLng = Math.round(loc.getLongitude() / 2) * 2;
		WindData keygen = new WindData(lvl, rLat, rLng);
		String key = String.valueOf(keygen.cacheKey());
		
		try {
			setBucket("winds", lvl.toString());
			return (WindData) JedisUtils.get(createKey(key));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
// Copyright 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

import org.deltava.util.MemcachedUtils;

/**
 * A Data Access Object to load winds aloft data from memcached.
 * @author Luke
 * @version 6.1
 * @since 5.4
 */

public class GetWinds extends MemcachedDAO {

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
			return (WindData) MemcachedUtils.get(createKey(key), 100);
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.concurrent.*;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to load winds aloft data from memcached.
 * @author Luke
 * @version 6.0
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
		
		Future<Object> f = null;
		try {
			checkConnection();
			setBucket("winds", lvl.toString());
			f = _client.asyncGet(createKey(key));
			return (WindData) f.get(100, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			cancel(f);
		}
		
		throw new UnsupportedOperationException();
	}
}
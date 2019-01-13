// Copyright 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to save temporary ACARS track data to Redis.
 * @author Luke
 * @version 8.5
 * @since 7.0
 */

public class SetTrack extends RedisDAO {

	private static final Logger log = Logger.getLogger(SetTrack.class);
	private static final Cache<CacheableList<GeoLocation>> _casCache = CacheManager.getCollection(GeoLocation.class, "ACARSTrackCAS");

	/**
	 * Adds a route entry to Redis.
	 * @param isACARS TRUE if ACARS, FALSE if simFDR
	 * @param flightID the Flight ID
	 * @param gl a GeoLocation
	 */
	@SuppressWarnings("unchecked")
	public void write(boolean isACARS, String flightID, GeoLocation gl) {
		setBucket("track", isACARS ? "acars" : "simFDR");
		String rawKey = flightID.intern();
		String key = createKey(rawKey);

		try {
			synchronized (rawKey) {
				CacheableList<GeoLocation> data = _casCache.get(rawKey);
				if (data == null)
					data = (CacheableList<GeoLocation>) RedisUtils.get(key);
				if (data == null)
					data = new CacheableList<GeoLocation>(rawKey);
				
				// Check to make sure position has changed
				int distance = data.isEmpty() ? Integer.MAX_VALUE : GeoUtils.distanceFeet(gl, data.get(data.size() - 1));
				if (distance > 10)
					data.add(new GeoPosition(gl));
				
				_casCache.add(data);
				RedisUtils.delete(key);
				RedisUtils.write(key, 3600, data);
			}
		} catch (Exception e) {
			log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
		}
	}

	/**
	 * Deletes a track from the cache.
	 * @param isACARS TRUE if ACARS, FALSE if simFDR
	 * @param flightID the Flight ID
	 */
	public void clear(boolean isACARS, String flightID) {
		try {
			setBucket("track", isACARS ? "acars" : "simFDR");
			RedisUtils.delete(createKey(flightID));
		} catch (Exception e) {
			log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
		} finally {
			_casCache.remove(flightID);	
		}
	}
}
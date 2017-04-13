// Copyright 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to save temporary ACARS track data to Redis.
 * @author Luke
 * @version 7.3
 * @since 7.0
 */

public class SetTrack extends RedisDAO {

	private static final Logger log = Logger.getLogger(SetTrack.class);
	private static final Cache<CacheableCollection<GeoLocation>> _casCache = CacheManager.getCollection(GeoLocation.class, "ACARSTrackCAS");

	/**
	 * Adds a route entry to Redis.
	 * @param flightID the Flight ID
	 * @param gl a GeoLocation
	 */
	@SuppressWarnings("unchecked")
	public void write(int flightID, GeoLocation gl) {
		setBucket("acarsTrack");
		String rawKey = String.valueOf(flightID);
		String key = createKey(rawKey);

		try {
			synchronized (rawKey) {
				CacheableCollection<GeoLocation> data = _casCache.get(rawKey);
				if (data == null)
					data = (CacheableCollection<GeoLocation>) RedisUtils.get(key);
				if (data == null)
					data = new CacheableList<GeoLocation>(rawKey);

				data.add(new GeoPosition(gl));
				_casCache.add(data);
				RedisUtils.write(key, 900, data);
			}
		} catch (Exception e) {
			log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
		}
	}

	/**
	 * Deletes a track from the cache.
	 * @param flightID the Flight ID
	 */
	public void clear(int flightID) {
		String rawKey = String.valueOf(flightID);
		try {
			setBucket("acarsTrack");
			RedisUtils.delete(createKey(rawKey));
		} catch (Exception e) {
			log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
		} finally {
			_casCache.remove(rawKey);	
		}
	}
}
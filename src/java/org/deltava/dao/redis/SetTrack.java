// Copyright 2016, 2017, 2018, 2019, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.Collection;

import org.apache.logging.log4j.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.TrackUpdate;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import redis.clients.jedis.*;

/**
 * A Data Access Object to save temporary ACARS track data to Redis.
 * @author Luke
 * @version 11.3
 * @since 7.0
 */

public class SetTrack extends RedisDAO {

	private static final Logger log = LogManager.getLogger(SetTrack.class);
	private static final Cache<CacheableList<GeoLocation>> _casCache = CacheManager.getCollection(GeoLocation.class, "ACARSTrackCAS");

	/**
	 * Adds route entries to Redis.
	 * @param upds a Collection of TrackUpdate beans
	 */
	@SuppressWarnings("unchecked")
	public void write(Collection<TrackUpdate> upds) {
		for (TrackUpdate upd : upds) {
			setBucket("track", upd.isACARS() ? "acars" : "simFDR");
			String key = createKey(upd.getFlightID());
			CacheableList<GeoLocation> data = _casCache.get(key);
			if (data == null)
				data = (CacheableList<GeoLocation>) RedisUtils.get(key);
			if (data == null)
				data = new CacheableList<GeoLocation>(key);
		
			// Check to make sure position has changed
			int distance = data.isEmpty() ? Integer.MAX_VALUE : upd.getLocation().distanceFeet(data.getLast());
			if (distance > 10)
				data.add(new GeoPosition(upd));

			_casCache.add(data);
			byte[] rawKey = RedisUtils.encodeKey(key);
			try (Jedis j = SystemData.getJedisPool().getConnection()) {
				Pipeline jp = j.pipelined();
				j.del(rawKey);
				j.set(rawKey, RedisUtils.write(data));
				j.expireAt(rawKey, 3600);
				jp.sync();
			} catch (Exception e) {
				log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
			}
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
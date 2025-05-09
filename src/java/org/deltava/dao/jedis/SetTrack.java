// Copyright 2016, 2017, 2018, 2019, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.jedis;

import java.util.Collection;

import org.apache.logging.log4j.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.TrackUpdate;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

/**
 * A Data Access Object to save temporary ACARS track data to Redis.
 * @author Luke
 * @version 11.6
 * @since 7.0
 */

public class SetTrack extends JedisDAO {

	private static final Logger log = LogManager.getLogger(SetTrack.class);
	private static final Cache<CacheableList<GeoLocation>> _casCache = CacheManager.getCollection(GeoLocation.class, "ACARSTrackCAS");

	/**
	 * Adds route entries to Redis.
	 * @param upds a Collection of TrackUpdate beans
	 */
	@SuppressWarnings("unchecked")
	public void write(Collection<TrackUpdate> upds) {
		try (Jedis j = SystemData.getJedisPool().getConnection()) {
			Pipeline jp = j.pipelined();
			for (TrackUpdate upd : upds) {
				setBucket("track", upd.isACARS() ? "acars" : "simFDR");
				String key = createKey(upd.getFlightID());
				
				// Try L1 cache
				CacheableList<GeoLocation> data = _casCache.get(String.valueOf(upd.getFlightID()));
				if (data == null)
					data = (CacheableList<GeoLocation>) JedisUtils.get(key);
				if (data == null)
					data = new CacheableList<GeoLocation>(key);
			
				// Check to make sure position has changed
				int distance = data.isEmpty() ? Integer.MAX_VALUE : upd.getLocation().distanceFeet(data.getLast());
				if (distance > 10)
					data.add(new GeoPosition(upd));

				byte[] rawKey = JedisUtils.encodeKey(key);
				_casCache.add(data);
				j.set(rawKey, JedisUtils.write(data), SetParams.setParams().ex(3600));
			}
			
			jp.sync();
		} catch (Exception e) {
			log.error("Error flushing positions - {}", e.getMessage());
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
			JedisUtils.delete(createKey(flightID));
		} catch (Exception e) {
			log.warn(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage());
		} finally {
			_casCache.remove(flightID);	
		}
	}
}
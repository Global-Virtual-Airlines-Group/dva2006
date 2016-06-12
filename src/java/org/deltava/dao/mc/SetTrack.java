// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.MemcachedUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to save temporary ACARS track data to memcached.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SetTrack extends MemcachedDAO {
	
	private static final Logger log = Logger.getLogger(SetTrack.class);
	
	private static final Cache<CacheableCollection<GeoLocation>> _casCache = CacheManager.getCollection(GeoLocation.class, "ACARSTrackCAS");

	/**
	 * Adds a route entry to memcached.
	 * @param flightID the Flight ID
	 * @param gl a GeoLocation
	 */
	@SuppressWarnings("unchecked")
	public void write(int flightID, GeoLocation gl) {
		setBucket("acarsTrack");
		String rawKey = String.valueOf(flightID);
		String key = createKey(rawKey);
		
	    try {
			CacheableCollection<GeoLocation> data = _casCache.get(rawKey);
	    	if (data == null)
	    		data = (CacheableCollection<GeoLocation>) MemcachedUtils.get(key, 175, false);
	    	if (data == null)
	    		data = new CacheableList<GeoLocation>(rawKey);
	    	
	    	data.add(new GeoPosition(gl));
	    	_casCache.add(data);
	    	MemcachedUtils.write(key, 900, data);
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
		setBucket("acarsTrack");
		MemcachedUtils.delete(createKey(rawKey));
		_casCache.remove(rawKey);
	}
}
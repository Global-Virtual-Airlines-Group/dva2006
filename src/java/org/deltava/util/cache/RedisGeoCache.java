// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.GeoLocation;

import org.deltava.util.RedisUtils;

/**
 * A cache to store geographic lookups in Redis. Lat/long coordinates are automatically reduced to a set number of decimal places. 
 * @author Luke
 * @version 7.4
 * @since 7.3
 * @param <T> the Cacheable object type
 */

public class RedisGeoCache<T extends Cacheable> extends RedisCache<T> implements GeoCache<T> {

	private final double _roundAmt;
	
	/**
	 * Creates the cache.
	 * @param bucket the Redis bucket name 
	 * @param expiry the expirty time in seconds
	 * @param roundAmt the rounding factot to apply to latitude/longitude
	 */
	public RedisGeoCache(String bucket, int expiry, double roundAmt) {
		super(bucket, expiry);
		_roundAmt = Math.max(0.000001, roundAmt);
	}
	
	@Override
	public double getRoundingAmount() {
		return _roundAmt;
	}
	
	/**
	 * Adds a geographically located object to the cache.
	 * @param loc the GeoLocation
	 * @param data the data to cache
	 */
	@Override
	public void add(GeoLocation loc, T data) {
		if (data == null) return;
		RedisUtils.write(createKey(createGeoKey(loc)), getExpiryTime(data), new RemoteCacheEntry<T>(data));
	}
	
	/**
	 * Adds a null entry to the cache.
	 * @param loc the GeoLocation
	 */
	@Override
	public void addNull(GeoLocation loc) {
		if (loc == null) return;
		addNullEntry(createGeoKey(loc));
	}
	
	/**
	 * Returns whether the cache contains an entry for a particular location. 
	 * @param loc the GeoLocation
	 * @return TRUE if the cache contains an entry, otherwise FALSE
	 */
	@Override
	public boolean contains(GeoLocation loc) {
		return (loc == null) ? false : contains(createGeoKey(loc));
	}
	
	/**
	 * Retrieves an object from the cache.
	 * @param loc the GeoLocation
	 * @return the Object, or null if not found
	 */
	@Override
	public T get(GeoLocation loc) {
		return (loc == null) ? null : get(createGeoKey(loc));
	}
	
	/**
	 * Removes a location entry from the cache.
	 * @param loc the GeoLocation
	 */
	@Override
	public void remove(GeoLocation loc) {
		if (loc != null) remove(createGeoKey(loc));
	}
}
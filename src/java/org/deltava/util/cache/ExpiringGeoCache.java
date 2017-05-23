// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.GeoLocation;

/**
 * A cache to store geographic lookups. Lat/long coordinates are automatically reduced to a set number of decimal places.  
 * @author Luke
 * @version 7.4
 * @since 7.3
 * @param <T> the Cacheable object type 
 */

public class ExpiringGeoCache<T extends Cacheable> extends ExpiringCache<T> implements GeoCache<T> {
	
	private final double _roundAmt;

	/**
	 * Creates a new Cache.
	 * @param maxSize the maximum number of entries
	 * @param expiryTime the expiration time in seconds
	 * @param roundAmt the number of decimal places to round to
	 * @see ExpiringCache#setMaxSize(int)
	 * @see ExpiringCache#setExpiration(int)
	 */
	public ExpiringGeoCache(int maxSize, int expiryTime, double roundAmt) {
		super(maxSize, expiryTime);
		_roundAmt = Math.max(0.00001, roundAmt);
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
		
		// Create the cache entry
		ExpiringCacheEntry<T> e = new ExpiringLocalCacheEntry<T>(data);
		_cache.put(createGeoKey(loc), e);
		checkOverflow();
	}

	/**
	 * Adds a null entry to the cache.
	 * @param loc the GeoLocation
	 */
	@Override
	public void addNull(GeoLocation loc) {
		if (loc == null) return;
		addNullEntry(createGeoKey(loc));
		checkOverflow();
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
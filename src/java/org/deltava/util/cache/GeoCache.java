// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.math.*;

import org.deltava.beans.GeoLocation;

/**
 * An interface for caches that cache data based on geographic locations.
 * @author Luke
 * @version 8.1
 * @since 7.3
 * @param <T> the Cacheable object type 
 */

public interface GeoCache<T extends Cacheable> {
	
	/**
	 * Returns the precision of the lat/long key rounding.
	 * @return the rounding amount
	 */
	public double getRoundingAmount();

	/**
	 * Adds a geographically located object to the cache.
	 * @param loc the GeoLocation
	 * @param data the data to cache
	 */
	public void add(GeoLocation loc, T data);
	
	/**
	 * Adds a null entry to the cache.
	 * @param loc the GeoLocation
	 */
	public void addNull(GeoLocation loc);
	
	/**
	 * Returns whether the cache contains an entry for a particular location. 
	 * @param loc the GeoLocation
	 * @return TRUE if the cache contains an entry, otherwise FALSE
	 */
	public boolean contains(GeoLocation loc);
	
	/**
	 * Retrieves an object from the cache.
	 * @param loc the GeoLocation
	 * @return the Object, or null if not found
	 */
	public T get(GeoLocation loc);
	
	/**
	 * Removes a location entry from the cache.
	 * @param loc the GeoLocation
	 */
	public void remove(GeoLocation loc);

	/**
	 * Creates a lat/long key by rounding to the specific number of decimal places. 
	 * @param loc the GeoLocation
	 * @return a key
	 */
	default String createGeoKey(GeoLocation loc) {
		BigDecimal inc = new BigDecimal(Double.toString(getRoundingAmount()));
		BigDecimal lat = new BigDecimal(Double.toString(loc.getLatitude())).divide(inc, 0, RoundingMode.HALF_DOWN).multiply(inc);
		BigDecimal lng = new BigDecimal(Double.toString(loc.getLongitude())).divide(inc, 0, RoundingMode.HALF_DOWN).multiply(inc);
		StringBuilder buf = new StringBuilder(lat.toString());
		buf.append('@').append(lng.toString());
		return buf.toString();
	}
}
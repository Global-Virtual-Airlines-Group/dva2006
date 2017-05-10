// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.GeoLocation;

/**
 * An interface for caches that cache data based on geographic locations.
 * @author Luke
 * @version 7.3
 * @since 7.3
 * @param <T> the Cacheable object type 
 */

public interface GeoCache<T extends Cacheable> {

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
}
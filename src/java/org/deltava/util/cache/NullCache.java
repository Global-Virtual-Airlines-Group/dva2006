// Copyright 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.GeoLocation;

/**
 * A cache implementation that does no caching. This is used to ensure that CacheManager never returns null. 
 * @author Luke
 * @version 7.4
 * @since 5.0
 * @param <T> the Cacheable object type
 */

public class NullCache<T extends Cacheable> extends Cache<T> implements GeoCache<T> {
	
	/**
	 * Creates a new null cache.
	 */
	NullCache() {
		super(1);
	}
	
	/**
	 * Returns the maximum size of the cache.
	 * @return the maximum number of entries in the cache
	 */
	@Override
	public final int getMaxSize() {
		return 0;
	}
	
	@Override
	protected void addEntry(T entry) {
		// empty
	}

	@Override
	protected void addNullEntry(Object key) {
		// empty
	}

	@Override
	public T get(Object key) {
		request();
		return null;
	}
	
	@Override
	public double getRoundingAmount() {
		return 0.01;
	}

	@Override
	public void add(GeoLocation loc, T data) {
		// empty
	}

	@Override
	public void addNull(GeoLocation loc) {
		// empty
	}

	@Override
	public boolean contains(GeoLocation loc) {
		return false;
	}

	@Override
	public T get(GeoLocation loc) {
		return null;
	}

	@Override
	public void remove(GeoLocation loc) {
		// empty
	}
}
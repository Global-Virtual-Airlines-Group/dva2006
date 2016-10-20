// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A cache implementation that does no caching. This is used to ensure that CacheManager never returns null. 
 * @author Luke
 * @version 7.2
 * @since 5.0
 * @param <T> the Cacheable object type
 */

public class NullCache<T extends Cacheable> extends Cache<T> {
	
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

	/**
	 * Adds an entry to the cache - not implemented.
	 */
	@Override
	protected void addEntry(T entry) {
		// empty
	}

	/**
	 * Adds an entry to the cache - not implemented.
	 */
	@Override
	protected void addNullEntry(Object key) {
		// empty
	}

	/**
	 * Retrieves an entry from the cache - not implemented.
	 * @return null
	 */
	@Override
	public T get(Object key) {
		request();
		return null;
	}
}
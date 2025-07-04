// Copyright 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A cache entry stored in a remote (Redis/memcached) cache.
 * @author Luke
 * @version 7.3
 * @since 6.1
 * @param <T> the cacheable object type
 */

class RemoteCacheEntry<T extends Cacheable> extends CacheEntry<T> {
	
	/**
	 * Creates a new cache entry.
	 * @param entry the cache object
	 */
	public RemoteCacheEntry(T entry) {
		super(entry);
		_createExpire = System.currentTimeMillis();
	}
	
	/**
	 * Creates a new cache entry.
	 * @param key the cache key
	 * @param entry the cache object
	 */
	public RemoteCacheEntry(String key, T entry) {
		super(key, entry);
		_createExpire = System.currentTimeMillis();
	}
	
	/**
	 * Returns the creation date of the cache entry.
	 * @return the creation date
	 */
	public long getCreatedOn() {
		return _createExpire;
	}
}
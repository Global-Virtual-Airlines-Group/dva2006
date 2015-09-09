// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

class MemcachedCacheEntry<T extends Cacheable> extends CacheEntry<T> {
	
	/**
	 * Creates a new memcached cache entry.
	 * @param entry the cache entry
	 */
	public MemcachedCacheEntry(T entry) {
		super(entry);
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
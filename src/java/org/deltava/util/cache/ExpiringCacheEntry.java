// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A cache entry with an expiration date.
 * @author Luke
 * @version 7.3
 * @param <T> the Cacheable object type
 * @since 1.0
 */

abstract class ExpiringCacheEntry<T extends Cacheable> extends CacheEntry<T> {
	
	/**
	 * Creates a new cache entry.
	 * @param entryData the entry data
	 */
	protected ExpiringCacheEntry(T entryData) {
		super(entryData);
	}

	/**
	 * Creates a null cache entry.
	 * @param key the cache key
	 */
	protected ExpiringCacheEntry(Object key) {
		super(key, null);
	}

	/**
	 * Returns whether the entry has expired.
	 * @return TRUE if expired, otherwise FALSE
	 */
	public boolean isExpired() {
		return (_createExpire < System.currentTimeMillis());
	}
}
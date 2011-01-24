// Copyright 2007, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.lang.ref.*;

/**
 * An abstract class to handle cache entries.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

abstract class CacheEntry<T extends Cacheable> extends SoftReference<T> implements Comparable<CacheEntry<T>> {
	
	private Object _key;

	/**
	 * Initializes the entry.
	 * @param entry the cached data
	 * @param q the reference queue
	 */
	CacheEntry(T entry, ReferenceQueue<? super T> q) {
		super(entry, q);
		if (entry != null)
			_key = entry.cacheKey();
	}
	
	/**
	 * Returns the object's cache key.
	 * @return the key
	 */
	Object getKey() {
		return _key;
	}

	/**
	 * Returns the string representation of the cache key.
	 */
	public String toString() {
		return _key.toString();
	}
	
	public int hashCode() {
		return _key.hashCode();
	}
}
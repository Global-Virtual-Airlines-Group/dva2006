// Copyright 2007, 2008, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An abstract class to handle cache entries.
 * @author Luke
 * @version 8.0
 * @since 1.0
 * @param <T> the cacheable object type
 */

abstract class CacheEntry<T extends Cacheable> implements java.io.Serializable, Comparable<CacheEntry<T>> {
	
	private final Object _key;
	private final T _entry;
	
	/**
	 * A Helper variable used to track either creation or expiry time.
	 */
	protected long _createExpire;

	/**
	 * Initializes the entry.
	 * @param entry the cached data
	 */
	CacheEntry(T entry) {
		super();
		_entry = entry;
		_key =  (entry != null) ? entry.cacheKey() : null;
	}
	
	/**
	 * Initializes the key and the entry.
	 * @param key the key
	 * @param entry the entry
	 */
	CacheEntry(Object key, T entry) {
		super();
		_entry = entry;
		_key = key;
	}
	
	/**
	 * Returns the object's cache key.
	 * @return the key
	 */
	Object getKey() {
		return _key;
	}
	
	/**
	 * Returns the cached object.
	 * @return the object
	 */
	public T get() {
		return _entry;
	}

	/**
	 * Returns the string representation of the cache key.
	 */
	@Override
	public String toString() {
		return _key.toString();
	}
	
	@Override
	public int hashCode() {
		return _key.hashCode();
	}

	@Override
	public int compareTo(CacheEntry<T> e2) {
		return Long.compare(_createExpire, e2._createExpire);
	}
}
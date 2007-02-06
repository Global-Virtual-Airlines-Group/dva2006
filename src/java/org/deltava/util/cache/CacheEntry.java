// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An abstract class to handle cache entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

abstract class CacheEntry<T extends Cacheable> implements Comparable {
	
	private T _data;

	/**
	 * Initializes the entry.
	 * @param entry the cached data
	 */
	protected CacheEntry(T entry) {
		super();
		_data = entry;
	}

	/**
	 * Returns the cached object.
	 * @return the cached data
	 */
	public T getData() {
		return _data;
	}
}
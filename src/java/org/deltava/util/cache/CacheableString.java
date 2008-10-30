// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable String.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class CacheableString implements Cacheable {
	
	private Object _key;
	private String _data;

	/**
	 * Creates the Object.
	 * @param cacheKey the cache key
	 * @param value the string to cache
	 */
	public CacheableString(Object cacheKey, String value) {
		super();
		_key = cacheKey;
		_data = value;
	}

	/**
	 * Returns the cache key.
	 */
	public Object cacheKey() {
		return _key;
	}

	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return _data;
	}
	
	public String toString() {
		return _data;
	}
}
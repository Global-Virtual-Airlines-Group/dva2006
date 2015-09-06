// Copyright 2008, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable String.
 * @author Luke
 * @version 6.1
 * @since 2.2
 */

public class CacheableString implements Cacheable {
	
	private final Object _key;
	private final String _data;

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
	@Override
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
	
	@Override
	public int hashCode() {
		return _data.hashCode();
	}

	@Override
	public String toString() {
		return _data;
	}
	
	@Override
	public boolean equals(Object o) {
		return String.valueOf(o).equals(_data);
	}
}
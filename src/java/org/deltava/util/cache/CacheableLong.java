// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable Long.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CacheableLong implements Cacheable {

	private Object _key;
	private long _value;

	/**
	 * Creates the Object.
	 * @param cacheKey the cache key
	 * @param value the long value
	 */
	public CacheableLong(Object cacheKey, long value) {
		super();
		_key = cacheKey;
		_value = value;
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
	public long getValue() {
		return _value;
	}
	
	public int hashCode() {
		return _key.hashCode();
	}
}
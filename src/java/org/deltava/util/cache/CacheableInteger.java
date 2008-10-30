// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable Integer.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CacheableInteger implements Cacheable {

	private Object _key;
	private int _value;

	/**
	 * Creates the Object.
	 * @param cacheKey the cache key
	 * @param value the integer value
	 */
	public CacheableInteger(Object cacheKey, int value) {
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
	public int getValue() {
		return _value;
	}
}
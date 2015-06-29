// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable Object.
 * @author Luke
 * @version 6.0
 * @since 6.0
 */

public class CacheWrapper<T extends Object> implements Cacheable {

	private final Object _key;
	private final T _value;

	/**
	 * Creates the Object.
	 * @param cacheKey the cache key
	 * @param value the long value
	 */
	public CacheWrapper(Object cacheKey, T value) {
		super();
		_key = cacheKey;
		_value = value;
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
	public T getValue() {
		return _value;
	}
	
	@Override
	public int hashCode() {
		return _key.hashCode();
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}
}
// Copyright 2005, 2006, 2009, 2012, 2015, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A utility class to create a cacheable Long.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class CacheableLong implements Cacheable {

	private final Object _key;
	private final long _value;

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
	@Override
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
	
	/**
	 * Returns the value as a 32-bit integer.
	 * @return an int
	 */
	public int intValue() {
		return (int) _value;
	}
	
	@Override
	public int hashCode() {
		return _key.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CacheableLong cl2) && (cl2._value == _value);
	}
	
	@Override
	public String toString() {
		return Long.toString(_value);
	}
}
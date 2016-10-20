// Copyright 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.deltava.util.cache.Cacheable;

/**
 * A utility class to create a tuple. 
 * @author Luke
 * @version 7.2
 * @since 6.0
 * @param <K> the first value type
 * @param <V> the second value type
 */

public class Tuple<K, V> implements Cacheable {

	private final K _k;
	private final V _v;

	private Tuple(K k, V v) {
		super();
		_k = k;
		_v = v;
	}
	
	public K getLeft() {
		return _k;
	}
	
	public V getRight() {
		return _v;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(_k));
		buf.append('#').append(String.valueOf(_v));
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public final static <K, V> Tuple<K, V> create(K k, V v) {
		return new Tuple<K, V>(k, v);
	}

	@Override
	public Object cacheKey() {
		return toString();
	}
}
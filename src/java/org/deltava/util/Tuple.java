// Copyright 2014, 2016, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.deltava.util.cache.Cacheable;

/**
 * A utility class to create a tuple. 
 * @author Luke
 * @version 12.4
 * @since 6.0
 * @param <K> the first value type
 * @param <V> the second value type
 */

public class Tuple<K, V> implements Cacheable, Comparable<Tuple<K, ?>> {

	private final K _k;
	private final V _v;

	/**
	 * Constructor.
	 * @param k the first value
	 * @param v the second value
	 */
	protected Tuple(K k, V v) {
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
	
	public final static <K, V> Tuple<K, V> create(K k, V v) {
		return new Tuple<K, V>(k, v);
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
	
	@Override
	public Object cacheKey() {
		return toString();
	}

	@Override
	public int compareTo(Tuple<K, ?> t2) {
		return toString().compareTo(t2.toString());
	}
}
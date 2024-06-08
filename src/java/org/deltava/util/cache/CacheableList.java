// Copyright 2007, 2010, 2012, 2016, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;

/**
 * A utility class to create a cacheable List.
 * @author Luke
 * @version 11.2
 * @since 1.1
 * @see CacheableSet
 * @param <E> the cacheable object type
 */

public class CacheableList<E> extends ArrayList<E> implements CacheableCollection<E> {

	private final Object _key;
	
	/**
	 * Initializes the cachable List.
	 * @param key the cache key
	 */
	public CacheableList(Object key) {
		super();
		_key = key;
	}
	
	/**
	 * Converts a Collection into a cacheable List.
	 * @param key the cache key
	 * @param c the Collection
	 */
	public CacheableList(Object key, Collection<E> c) {
		this(key);
		addAll(c);
	}
	
	/**
	 * Copy constructor.
	 * @param cc the original CacheableCollection
	 */
	public CacheableList(CacheableCollection<E> cc) {
		this(cc.cacheKey());
		addAll(cc);
	}

	@Override
	public Object cacheKey() {
		return _key;
	}
	
	@Override
	public CacheableList<E> clone() {
		return new CacheableList<E>(this);
	}
}
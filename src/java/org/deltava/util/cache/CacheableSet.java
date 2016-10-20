// Copyright 2006, 2007, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.LinkedHashSet;

/**
 * A utility class to create a cacheable Set.
 * @author Luke
 * @version 7.2
 * @since 1.0
 * @see CacheableList
 * @param <E> the cacheable object type
 */

public class CacheableSet<E> extends LinkedHashSet<E> implements CacheableCollection<E> {
	
	private final Object _key;
	
	/**
	 * Initializes the cachable set.
	 * @param key the cache key
	 */
	public CacheableSet(Object key) {
		super();
		_key = key;
	}
	
	/**
	 * Copy constructor.
	 * @param cc the original CacheableCollection
	 */
	public CacheableSet(CacheableCollection<E> cc) {
		this(cc.cacheKey());
		addAll(cc);
	}
	
	@Override
	public Object cacheKey() {
		return _key;
	}
	
	@Override
	public CacheableSet<E> clone() {
		return new CacheableSet<E>(this);
	}
}
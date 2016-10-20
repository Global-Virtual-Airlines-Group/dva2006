// Copyright 2007, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.ArrayList;

/**
 * A utility class to create a cacheable List.
 * @author Luke
 * @version 7.2
 * @since 1.1
 * @see CacheableSet
 * @param <E> the cacheable object type
 */

public class CacheableList<E> extends ArrayList<E> implements CacheableCollection<E> {

	private final Object _key;
	
	/**
	 * Initializes the cachable list.
	 * @param key the cache key
	 */
	public CacheableList(Object key) {
		super();
		_key = key;
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
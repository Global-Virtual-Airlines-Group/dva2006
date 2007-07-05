// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.ArrayList;

/**
 * A utility class to create a cacheable List.
 * @author Luke
 * @version 1.1
 * @since 1.1
 * @see CacheableSet
 */

public class CacheableList<E> extends ArrayList<E> implements CacheableCollection<E> {

	private Object _key;
	
	/**
	 * Initializes the cachable list.
	 * @param key the cache key
	 */
	public CacheableList(Object key) {
		super();
		_key = key;
	}

	/**
	 * Returns the cache key.
	 */
	public Object cacheKey() {
		return _key;
	}
}
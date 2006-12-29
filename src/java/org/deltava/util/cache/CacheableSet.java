// Copyright 2006, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.LinkedHashSet;

/**
 * A utility class to create a cacheable Set.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CacheableSet<E> extends LinkedHashSet<E> implements Cacheable {
	
	private Object _key;
	
	/**
	 * Initializes the cachable set.
	 * @param key the cache key
	 */
	public CacheableSet(Object key) {
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
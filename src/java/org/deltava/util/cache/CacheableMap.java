// Copyright 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;

/**
 * A utility class to create a cacheable Map.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class CacheableMap<K, V> extends LinkedHashMap<K, V> implements Cacheable {
	
	private final Object _key;

	/**
	 * Initializes the cachable map.
	 * @param key the cache key
	 */
	public CacheableMap(Object key) {
		super();
		_key = key;
	}

	/**
	 * Returns the cache key.
	 */
	@Override
	public Object cacheKey() {
		return _key;
	}
}
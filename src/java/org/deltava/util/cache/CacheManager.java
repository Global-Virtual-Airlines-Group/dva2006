// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.*;

/**
 * A utility class to handle centralized cache registration and invalidation.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class CacheManager {

	private static final Logger log = Logger.getLogger(CacheManager.class);
	private static final ConcurrentMap<String, Cache<?>> _caches = new ConcurrentHashMap<String, Cache<?>>(24);
	
	// singleton
	private CacheManager() {
		super();
	}
	
	/**
	 * Returns information about all caches.
	 * @return a Collection of CacheInfo objects
	 */
	public static Collection<CacheInfo> getCacheInfo() {
		Collection<Map.Entry<String, Cache<?>>> entries = new ArrayList<>(_caches.entrySet());
		Collection<CacheInfo> results = new ArrayList<CacheInfo>();
		for (Map.Entry<String, Cache<?>> me : entries)
			results.add(new CacheInfo(me.getKey(), me.getValue()));

		return results;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Cacheable> Cache<T> get(String id) {
		return (Cache<T>) _caches.get(id);
	}
	
	/**
	 * Invalidates a cache.
	 * @param id the cache ID
	 */
	public static void invalidate(String id) {
		Cache<?> cache = _caches.get(id);
		if (cache != null) cache.clear();
	}
	
	/**
	 * Invalidates a cache entry.
	 * @param id the cache ID
	 * @param key the cache key
	 */
	public static void invalidate(String id, Object key) {
		Cache<?> cache = _caches.get(id);
		if (cache != null) cache.remove(key);
	}
	
	/**
	 * Registers a cache. 
	 * @param c the cache content class
	 * @param id the cache ID
	 * @param maxSize the maximum size of the cache, in entries
	 * @param expiryTime the expiry time in seconds, zero or negative values return an AgingCache
	 * @return a Cache
	 * @see ExpiringCache
	 * @see AgingCache
	 */
	static <T extends Cacheable> Cache<T> register(Class<T> c, String id, int maxSize, int expiryTime) {
		Cache<T> cache = get(id);
		if (cache != null) {
			log.info("Duplicate registration attempted for cache " + id + "!");
			return cache;
		}
		
		// Create the cache
		if (maxSize < 1) {
			cache = new NullCache<T>();
			log.info("Registered cache " + id + ", null cache");
		} else if (expiryTime > 0) {
			cache = new ExpiringCache<T>(maxSize, expiryTime);
			log.info("Registered cache " + id + ", size=" + maxSize + ", expiry=" + expiryTime + "s");
		} else {
			cache = new AgingCache<T>(maxSize);
			log.info("Registered cache " + id + ", size=" + maxSize);
		}
		
		// Register and return
		_caches.put(id, cache);
		return cache;
	}
	
	/**
	 * Retrieves an existing cache. This will return a {@link NullCache} if the cache ID has not been registered. 
	 * @param c the cache content class
	 * @param id the cache ID
	 * @return a Cache
	 */
	public static <T extends Cacheable> Cache<T> get(Class<T> c, String id) {
		Cache<T> cache = get(id);
		return (cache != null) ? cache : register(c, id, -1, 0);
	}

	/**
	 * Retrieves an existing Collection cache. This will return a {@link NullCache} if the cache ID has not been registered. 
	 * @param c the cache Collection content class
	 * @param id the cache ID
	 * @return a Cache
	 */
	public static <U extends Object, T extends CacheableCollection<U>> Cache<T> getCollection(Class<U> c, String id) {
		Cache<T> cache = get(id);
		if (cache != null) return cache;

		// Register a null cache
		cache = new NullCache<T>();
		_caches.put(id, cache);
		return cache;
	}
}
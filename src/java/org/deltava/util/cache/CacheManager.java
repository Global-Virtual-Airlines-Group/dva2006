// Copyright 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;
import java.util.concurrent.locks.*;

import org.apache.log4j.*;

import org.gvagroup.common.*;

/**
 * A utility class to handle centralized cache registration and invalidation.
 * @author Luke
 * @version 7.2
 * @since 5.0
 */

public class CacheManager {

	private static final Logger log = Logger.getLogger(CacheManager.class);
	
	private static final ReentrantReadWriteLock _rw = new ReentrantReadWriteLock(true);
	private static final ReentrantReadWriteLock.ReadLock _r = _rw.readLock();
	private static final ReentrantReadWriteLock.WriteLock _w = _rw.writeLock();
	
	private static final Map<String, Cache<?>> _caches = new LinkedHashMap<String, Cache<?>>(32);
	
	// singleton
	private CacheManager() {
		super();
	}
	
	/**
	 * Returns information about all caches.
	 * @return a Collection of CacheInfo objects
	 */
	public static Collection<CacheInfo> getCacheInfo() {
		try {
			_r.lock();
			Collection<Map.Entry<String, Cache<?>>> entries = new ArrayList<>(_caches.entrySet());
			List<CacheInfo> results = new ArrayList<CacheInfo>(entries.size() + 2);
			entries.forEach(me -> results.add(new CacheInfo(me.getKey(), me.getValue())));
			Collections.reverse(results);
			return results;
		} finally {
			_r.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Cacheable> Cache<T> get(String id) {
		try {
			_r.lock();
			return (Cache<T>) _caches.get(id);
		} finally { 
			_r.unlock();
		}
	}
	
	/**
	 * Invalidates a cache.
	 * @param id the cache ID
	 */
	public static void invalidate(String id) {
		invalidate(id, true);
	}
	
	/**
	 * Invalidates a cache.
	 * @param id the cache ID
	 * @param sendEvent sends a system Event to other webapps if TRUE and remote 
	 */
	public static void invalidate(String id, boolean sendEvent) {
		Cache<?> cache = get(id);
		if (cache == null) {
			log.warn("Unknown cache - " + id);
			return;
		}
		
		cache.clear();
		if (sendEvent && (cache instanceof RedisCache))
			EventDispatcher.send(new IDEvent(SystemEvent.Type.CACHE_FLUSH, id));
	}
	
	/**
	 * Invalidates a cache entry.
	 * @param id the cache ID
	 * @param key the cache key
	 */
	public static void invalidate(String id, Object key) {
		Cache<?> cache = get(id);
		if (cache != null) cache.remove(key);
	}
	
	/*
	 * Helper method to add a cache.
	 */
	private static void addCache(String id, Cache<?> c) {
		try {
			_w.lock();
			_caches.put(id, c);
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Registers a cache. 
	 * @param c the cache content class
	 * @param id the cache ID
	 * @param maxSize the maximum size of the cache, in entries
	 * @param expiryTime the expiry time in seconds, zero or negative values return an AgingCache
	 * @param isRemote TRUE if a remote (Redis) cache, otherwise FALSE
	 * @return a Cache
	 * @see ExpiringCache
	 * @see AgingCache
	 */
	static <T extends Cacheable> Cache<T> register(Class<T> c, String id, int maxSize, int expiryTime, boolean isRemote) {
		Cache<T> cache = get(id);
		if (cache != null) {
			log.warn("Duplicate registration attempted for cache " + id + "!");
			return cache;
		}
		
		// Create the cache
		if (isRemote) {
			cache = new RedisCache<T>("cache:" + id, (expiryTime < 5) ? 86400 * 4 : expiryTime);
			log.info("Registered Redis cache " + id + ", expiry=" + expiryTime + "s");
		} else if (maxSize < 1) {
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
		addCache(id, cache);
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
		return (cache != null) ? cache : register(c, id, -1, 0, false);
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
		log.warn("Registering unknown null cache " + id);
		cache = new NullCache<T>();
		addCache(id, cache);
		return cache;
	}
	
	/**
	 * Retrieves an existing Map cache.  This will return a {@link NullCache} if the cache ID has not been registered.
	 * @param k the cache Map content key class
	 * @param v the cache Map content value class
	 * @param id the Cache ID
	 * @return a Cache
	 */
	public static <K extends Object, V extends Object, T extends CacheableMap<K, V>> Cache<T> getMap(Class<K> k, Class<V> v, String id) {
		Cache<T> cache = get(id);
		if (cache != null) return cache;
		
		// Register a null cache
		log.warn("Registering unknown null cache " + id);
		cache = new NullCache<T>();
		addCache(id, cache);
		return cache;		
	}
}
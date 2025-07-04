// Copyright 2012, 2015, 2016, 2017, 2018, 2021, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;
import java.util.concurrent.locks.*;

import org.apache.logging.log4j.*;

import org.gvagroup.common.*;

/**
 * A utility class to handle centralized cache registration and invalidation.
 * @author Luke
 * @version 11.5
 * @since 5.0
 */

public class CacheManager {

	private static final Logger log = LogManager.getLogger(CacheManager.class);
	
	private static final ReentrantReadWriteLock _rw = new ReentrantReadWriteLock(true);
	private static final Lock _r = _rw.readLock();
	private static final Lock _w = _rw.writeLock();
	
	private static String _id = "UNKNOWN";
	private static final Map<String, Cache<?>> _caches = new LinkedHashMap<String, Cache<?>>();
	
	// singleton
	private CacheManager() {
		super();
	}
	
	/**
	 * Initializes the cache Manager.
	 * @param id the application ID
	 */
	public static void init(String id) {
		try {
			_w.lock();
			_id = id;
			_caches.clear();
		} finally {
			_w.unlock();
		}
	}
	
	
	/**
	 * Returns information about all caches.
	 * @param getRemoteSize TRUE to fetch remote cache sizes, otherwise FALSE
	 * @return a Collection of CacheInfo objects
	 */
	public static Collection<CacheInfo> getCacheInfo(boolean getRemoteSize) {
		try {
			_r.lock();
			Collection<Map.Entry<String, Cache<?>>> entries = new ArrayList<>(_caches.entrySet());
			List<CacheInfo> results = new ArrayList<CacheInfo>(entries.size() + 2);
			entries.forEach(me -> results.add(new CacheInfo(me.getKey(), me.getValue(), getRemoteSize)));
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
			log.warn("Unknown {} cache - {}", _id, id);
			return;
		}
		
		cache.clear();
		if (sendEvent && cache.isRemote())
			EventDispatcher.send(new IDEvent(EventType.CACHE_FLUSH, id));
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
	 * @param cfg the CacheConfig bean
	 * @return a Cache
	 */
	static <T extends Cacheable> Cache<T> register(Class<T> c, CacheConfig cfg) {
		Cache<T> cache = get(cfg.getID());
		if (cache != null) {
			log.warn("Duplicate registration attempted for cache {}", cfg.getID());
			return cache;
		}
		
		// Create the cache
		if (cfg.getExpiryTime() == 0)
			return registerNull(cfg.getID());
		
		if (cfg.isGeo() && cfg.isRemote()) {
			cache = new JedisGeoCache<T>("cache:" + cfg.getID(), cfg.getExpiryTime(), cfg.getPrecision());
			log.info("{} registered GeoJedis cache {}, expiry={}s, precision={}", _id, cfg.getID(), Integer.valueOf(cfg.getExpiryTime()), Double.valueOf(cfg.getPrecision()));
		} else if (cfg.isRemote()) {
			cache = new JedisCache<T>("cache:" + cfg.getID(), cfg.getExpiryTime());
			log.info("{} registered Jedis cache {}, expiry={}s", _id, cfg.getID(), Integer.valueOf(cfg.getExpiryTime()));
		} else if (cfg.isGeo()) {
			cache = new ExpiringGeoCache<T>(cfg.getMaxSize(), cfg.getExpiryTime(), cfg.getPrecision());
			log.info("{} registered Geo cache {}, expiry={}s, precision={}", _id, cfg.getID(), Integer.valueOf(cfg.getExpiryTime()), Double.valueOf(cfg.getPrecision()));
		} else if (cfg.getExpiryTime() > 0) {
			cache = new ExpiringCache<T>(cfg.getMaxSize(), cfg.getExpiryTime());
			log.info("{} registered cache {}, size={}, expiry={}s", _id, cfg.getID(), Integer.valueOf(cfg.getMaxSize()), Integer.valueOf(cfg.getExpiryTime()));
		} else {
			cache = new AgingCache<T>(cfg.getMaxSize());
			log.info("{} registered cache {}, size={}", _id, cfg.getID(), Integer.valueOf(cfg.getMaxSize()));
		}
		
		addCache(cfg.getID(), cache);
		return cache;
	}
	
	/*
	 * Registers a null cache. 
	 */
	private static <T extends Cacheable> Cache<T> registerNull(String id) {
		Cache<T> cache = get(id);
		if (cache != null) {
			log.warn("Duplicate registration attempted by {} for cache {}", _id, id);
			return cache;
		}
		
		cache = new NullCache<T>();
		log.warn("{} registered cache {}, null cache", _id, id);
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
		return (cache != null) ? cache : registerNull(id);
	}
	
	/**
	 * Retrieves an existing GeoCache. This will return a {@link NullCache} if the cache ID has not been registered.
	 * @param c the cache content class
	 * @param id the cache ID
	 * @return a GeoCache
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Cacheable> GeoCache<T> getGeo(Class<T> c, String id) {
		GeoCache<T> cache = (GeoCache<T>) get(id);
		return (cache != null) ? cache : (GeoCache<T>) registerNull(id);
	}
	
	/**
	 * Retrieves an existing Collection cache. This will return a {@link NullCache} if the cache ID has not been registered. 
	 * @param c the cache Collection content class
	 * @param id the cache ID
	 * @return a Cache
	 */
	public static <U extends Object, T extends CacheableCollection<U>> Cache<T> getCollection(Class<U> c, String id) {
		Cache<T> cache = get(id);
		return (cache != null) ? cache : registerNull(id);
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
		return (cache != null) ? cache : registerNull(id);
	}
}
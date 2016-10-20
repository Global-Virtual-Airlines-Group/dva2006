// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An object cache that supports creation dates. The major difference between this cache and a {@link ExpiringCache} is
 * that this cache does not purge an entry until the cache overflows, whereas an {@link ExpiringCache} invalidates data
 * based on age.
 * @author Luke
 * @version 7.2
 * @since 1.0
 * @param <T> the Cacheable object type
 */

public class AgingCache<T extends Cacheable> extends Cache<T> {

	protected long _lastCreationTime;
	
	private final AgingCacheEntry<T> NULL_ENTRY = new AgingNullCacheEntry<T>();

	/**
	 * A cache entry for Aging caches.
	 * @param <U> the Cacheable object type
	 */
	protected class AgingCacheEntry<U extends T> extends CacheEntry<U> {

		public AgingCacheEntry(U entry) {
			super(entry);
			long now = System.currentTimeMillis();
			_createExpire = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
			_lastCreationTime = _createExpire;
		}
	}
	
	/**
	 * A null cache entry for Aging caches.
	 * @param <U> the Cacheable object type
	 */
	protected class AgingNullCacheEntry<U extends T> extends AgingCacheEntry<U> {
		
		AgingNullCacheEntry() {
			super(null);
		}
		
		@Override
		public String toString() {
			return "null";
		}
		
		@Override
		public int hashCode() {
			return "null".hashCode();
		}
	}

	/**
	 * Creates a new aging cache.
	 * @param maxSize the maximum size of the cache
	 * @throws IllegalArgumentException if maxSize is zero or negative
	 * @see AgingCache#setMaxSize(int)
	 */
	public AgingCache(int maxSize) {
		super(maxSize);
	}

	/**
	 * Adds an entry to the cache.
	 * @param obj the entry to add to the cache
	 */
	@Override
	protected void addEntry(T obj) {
		if (obj == null)
			return;

		// Create the cache entry
		AgingCacheEntry<T> e = new AgingCacheEntry<T>(obj);
		_cache.put(obj.cacheKey(), e);
	}
	
	/**
	 * Adds a null entry to the cache.
	 * @param key the entry key
	 */
	@Override
	protected void addNullEntry(Object key) {
		if (key == null)
			return;
		
		// Create the cache entry
		_cache.putIfAbsent(key, NULL_ENTRY);
	}

	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @return the cache entry, or null if not present
	 */
	@Override
	public T get(Object key) {
		request();
		if (key == null)
			return null;
		
		AgingCacheEntry<T> entry = (AgingCacheEntry<T>) _cache.get(key);
		if (entry == null)
			return null;

		hit();
		return entry.get();
	}
}
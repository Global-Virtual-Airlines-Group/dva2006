// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An object cache that supports creation dates. The major difference between this cache and a {@link ExpiringCache} is
 * that this cache does not purge an entry until the cache overflows, whereas an {@link ExpiringCache} invalidates data
 * based on age.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class AgingCache<T extends Cacheable> extends Cache<T> {

	protected long _lastCreationTime;

	protected class AgingCacheEntry<U extends Cacheable> extends CacheEntry<U> {

		private long _createdOn;

		public AgingCacheEntry(U entry) {
			super(entry);
			long now = System.currentTimeMillis();
			_createdOn = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
			_lastCreationTime = _createdOn;
		}

		public long getCreationTime() {
			return _createdOn;
		}

		public int compareTo(CacheEntry e2) {
			AgingCacheEntry ae2 = (AgingCacheEntry) e2;
			return new Long(_createdOn).compareTo(new Long(ae2._createdOn));
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
	 * Adds an entry to the cache. If this operation would cause the cache to exceed its maximum size, then the entry
	 * with the earliest creation date will be removed.
	 * @param obj the entry to add to the cache
	 */
	public void add(T obj) {
		if (obj == null)
			return;

		// Create the cache entry
		AgingCacheEntry<T> e = new AgingCacheEntry<T>(obj);
		_cache.put(obj.cacheKey(), e);

		// Check for overflow
		checkOverflow();
	}

	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @return the cache entry, or null if not present
	 */
	public T get(Object key) {
		request();
		AgingCacheEntry<T> entry = (AgingCacheEntry<T>) _cache.get(key);
		if (entry == null)
			return null;

		hit();
		return entry.getData();
	}
}
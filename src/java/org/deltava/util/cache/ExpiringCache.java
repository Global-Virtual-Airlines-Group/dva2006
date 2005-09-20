// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An object cache that supports expiration dates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExpiringCache extends Cache {

	protected long _lastCreationTime;
	protected int _expiry;

	protected class ExpiringCacheEntry implements Comparable {

		private Cacheable _entry;
		private long _expiryTime;

		public ExpiringCacheEntry(Cacheable entryData) {
			super();
			_entry = entryData;
			long now = System.currentTimeMillis();
			long createdOn = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
			_lastCreationTime = createdOn;
			_expiryTime = createdOn + _expiry;
		}

		public Cacheable getData() {
			return _entry;
		}

		public boolean isExpired() {
			return (_expiryTime < System.currentTimeMillis());
		}

		public long getExpiryTime() {
			return _expiryTime;
		}

		public int compareTo(Object o2) {
			ExpiringCacheEntry e2 = (ExpiringCacheEntry) o2;
			return new Long(_expiryTime).compareTo(new Long(e2.getExpiryTime()));
		}
	}

	/**
	 * Creates a new Cache.
	 * @param maxSize the maximum number of entries
	 * @param expiryTime the expiration time in seconds
	 * @throws IllegalArgumentException if maxSize or expiryTime are zero or negative
	 * @see ExpiringCache#setMaxSize(int)
	 * @see ExpiringCache#setExpiration(int)
	 */
	public ExpiringCache(int maxSize, int expiryTime) {
		super(maxSize);
		setExpiration(expiryTime);
	}

	/**
	 * Sets the cache's expiration interval.
	 * @param expiry the expiration interval in seconds
	 * @throws IllegalArgumentException if expiry is zero or negative
	 */
	public void setExpiration(int expiry) {
		if (expiry < 1)
			throw new IllegalArgumentException("Invalid expiration interval - " + expiry);

		_expiry = expiry * 1000;
	}

	/**
	 * Returns an unexpired entry from the cache.
	 * @param key the cache key
	 * @return the cache entry, or null if not present or expired
	 */
	public synchronized Cacheable get(Object key) {
	   request();
		ExpiringCacheEntry entry = (ExpiringCacheEntry) _cache.get(key);
		if (entry == null)
			return null;

		// If we're expired, remove the entry and return null
		if (entry.isExpired()) {
			_cache.remove(entry.getData().cacheKey());
			return null;
		}

		hit();
		return entry.getData();
	}

	/**
	 * Adds an entry to the cache. If this operation would cause the cache to exceed its maximum size, then the entry
	 * with the earliest expiration date will be removed. If the entry implements the {@link Cloneable} interface,
	 * then a clone of the entry will be added to the cache.
	 * @param obj the entry to add to the cache
	 */
	public synchronized void add(Cacheable obj) {

		// Create the cache entry
	   Cacheable entry = getClone(obj);
		ExpiringCacheEntry e = new ExpiringCacheEntry(entry);
		_cache.put(entry.cacheKey(), e);

		// Check for overflow
		checkOverflow();
	}
}
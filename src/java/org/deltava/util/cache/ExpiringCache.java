// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Iterator;

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
			if (entryData instanceof ExpiringCacheable) {
				_expiryTime = ((ExpiringCacheable) entryData).getExpiryDate().getTime();
			} else {
				_expiryTime = createdOn + _expiry;
			}
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
	
	public Cacheable get(Object key) {
		return get(key, false);
	}

	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @param ifExpired TRUE if expired entries can be returned, otherwise FALSE
	 * @return the cache entry, or null if not present or expired
	 * @see ExpiringCache#isExpired(Object)
	 */
	public synchronized Cacheable get(Object key, boolean ifExpired) {
		request();
		ExpiringCacheEntry entry = (ExpiringCacheEntry) _cache.get(key);
		if (entry == null)
			return null;

		// If we're expired, remove the entry and return null
		if (entry.isExpired() && !ifExpired) {
			_cache.remove(entry.getData().cacheKey());
			return null;
		}

		hit();
		return entry.getData();
	}
	
	/**
	 * Queries the cache to determine if an object has expired
	 * @param key the cache key
	 * @return TRUE if the object is present and expired, otherwise FALSE
	 */
	public synchronized boolean isExpired(Object key) {
		ExpiringCacheEntry entry = (ExpiringCacheEntry) _cache.get(key);
		return (entry == null) ? false : entry.isExpired();
	}

	/**
	 * Adds an entry to the cache. If this operation would cause the cache to exceed its maximum size, then the entry
	 * with the earliest expiration date will be removed.
	 * @param obj the entry to add to the cache
	 */
	public synchronized void add(Cacheable obj) {
		if (obj == null)
			return;

		// Create the cache entry
		ExpiringCacheEntry e = new ExpiringCacheEntry(obj);
		_cache.put(obj.cacheKey(), e);

		// Check for overflow and purge
		purge();
		checkOverflow();
	}

	/**
	 * Purges expired entries from the cache.
	 */
	private void purge() {
		for (Iterator i = _cache.values().iterator(); i.hasNext();) {
			ExpiringCacheEntry entry = (ExpiringCacheEntry) i.next();
			if (entry.isExpired())
				i.remove();
		}
	}
}
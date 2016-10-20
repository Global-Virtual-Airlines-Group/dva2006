// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * An object cache that supports expiration dates.
 * @author Luke
 * @version 7.2
 * @since 1.0
 * @param <T> the Cacheable object type
 */

public class ExpiringCache<T extends Cacheable> extends Cache<T> {

	private final Semaphore _pLock = new Semaphore(1, true);
	protected long _lastCreationTime;
	protected int _expiry;

	protected class ExpiringLocalCacheEntry<U extends T> extends ExpiringCacheEntry<U> {

		public ExpiringLocalCacheEntry(U entryData) {
			super(entryData);
			long now = System.currentTimeMillis();
			long createdOn = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
			_lastCreationTime = createdOn;
			if (entryData instanceof ExpiringCacheable)
				_createExpire = ((ExpiringCacheable) entryData).getExpiryDate().toEpochMilli();
			else
				_createExpire = createdOn + _expiry;
		}
	}
	
	/**
	 * A null cache entry for expiring caches.
	 * @param <U> the Cacheable object type
	 */
	protected class ExpiringNullCacheEntry<U extends T> extends ExpiringCacheEntry<U> {
		
		public ExpiringNullCacheEntry(Object key) {
			super(key);
			long now = System.currentTimeMillis();
			long createdOn = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
			_lastCreationTime = createdOn;
			_createExpire = createdOn + _expiry;
		}
		
		@Override
		public String toString() {
			return getKey().toString();
		}
		
		@Override
		public int hashCode() {
			return getKey().hashCode();
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
	 */
	public void setExpiration(int expiry) {
		_expiry = Math.max(1, expiry) * 1000;
	}

	/**
	 * Returns an unexpired entry from the cache.
	 * @param key the cache key
	 * @return the cache entry, or null if not present or expired
	 * @see ExpiringCache#get(Object, boolean)
	 */
	@Override
	public T get(Object key) {
		return get(key, false);
	}

	/**
	 * Returns if the cache contains a particular cache key <i>and the key has not expired</i>.
	 * @param key the cache key
	 * @return TRUE if the cache contains the key, otherwise FALSE
	 */
	@Override
	public boolean contains(Object key) {
		ExpiringCacheEntry<?> entry = (ExpiringCacheEntry<?>) _cache.get(key);
		return (entry != null) && (!entry.isExpired());
	}

	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @param ifExpired TRUE if expired entries can be returned, otherwise FALSE
	 * @return the cache entry, or null if not present
	 * @see ExpiringCache#get(Object)
	 * @see ExpiringCache#isExpired(Object)
	 */
	public T get(Object key, boolean ifExpired) {
		request();
		if (key == null)
			return null;
		
		ExpiringCacheEntry<T> entry = (ExpiringCacheEntry<T>) _cache.get(key);
		if (entry == null)
			return null;

		// If we're expired, remove the entry and return null
		if (entry.isExpired() && !ifExpired) {
			_cache.remove(entry.getKey());
			return null;
		}

		hit();
		return entry.get();
	}

	/**
	 * Queries the cache to determine if an object has expired
	 * @param key the cache key
	 * @return TRUE if the object is present and expired, otherwise FALSE
	 */
	public boolean isExpired(Object key) {
		ExpiringCacheEntry<T> entry = (ExpiringCacheEntry<T>) _cache.get(key);
		return (entry == null) ? false : entry.isExpired();
	}

	/**
	 * Adds an entry to the cache. If this operation would cause the cache to exceed its maximum size, then the entry
	 * with the earliest expiration date will be removed.
	 * @param obj the entry to add to the cache
	 */
	@Override
	protected void addEntry(T obj) {
		if (obj == null)
			return;

		// Create the cache entry
		ExpiringCacheEntry<T> e = new ExpiringLocalCacheEntry<T>(obj);
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
		ExpiringCacheEntry<T> e = new ExpiringNullCacheEntry<T>(key);
		_cache.putIfAbsent(key, e);
	}

	/**
	 * Purges expired entries from the cache.
	 */
	@Override
	protected void checkOverflow() {
		if (_pLock.tryAcquire()) {
			try {
				for (Iterator<CacheEntry<T>> i = _cache.values().iterator(); i.hasNext();) {
					ExpiringCacheEntry<T> entry = (ExpiringCacheEntry<T>) i.next();
					if (entry.isExpired())
						i.remove();
				}
			} finally {
				_pLock.release();
			}
		}
		
		super.checkOverflow();
	}
}
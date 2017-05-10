// Copyright 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.concurrent.atomic.LongAdder;

import org.deltava.util.RedisUtils;

/**
 * An object cache using Redis as its backing store.
 * @author Luke
 * @version 7.3
 * @since 7.1
 * @param <T> the Cacheable object type
 */

public class RedisCache<T extends Cacheable> extends Cache<T> {
	
	private final String _bucket;
	private final int _expiryTime;
	private long _lastFlushTime;
	private final LongAdder _errors = new LongAdder();
	
	/**
	 * Creates a new Cache.
	 * @param bucket the mecmached bucket to use
	 * @param expiry the expiration time in seconds
	 */
	public RedisCache(String bucket, int expiry) {
		super(1);
		_bucket = bucket;
		_expiryTime = Math.min(3600 * 24 * 30, Math.max(1, expiry));
	}
	
	/**
	 * Creates a Reids bucket:key key.
	 * @param key the raw key
	 * @return the bucket:key key
	 */
	protected String createKey(Object key) {
		StringBuilder buf = new StringBuilder(_bucket).append(':');
		return buf.append(String.valueOf(key)).toString();
	}
	
	/**
	 * Returns the expiration time for a cache entry, based on the cache or the entry's expiration time.
	 * @param entry the entry
	 * @return the time it will expire
	 */
	protected long getExpiryTime(T entry) {
		long expTime = _expiryTime;
		if (entry instanceof ExpiringCacheable) {
			ExpiringCacheable ec = (ExpiringCacheable) entry;
			expTime = ec.getExpiryDate().toEpochMilli();
		}
		
		return expTime;
	}
	
	/**
	 * Returns the number of cache errors.
	 * @return the number of errors
	 */
	@Override
	public long getErrors() {
		return _errors.longValue();
	}

	/**
	 * Adds an entry to the cache.
	 * @param entry the Cacheable object
	 */
	@Override
	protected void addEntry(T entry) {
		if (entry == null) return;
		RedisUtils.write(createKey(entry.cacheKey()), getExpiryTime(entry), new RemoteCacheEntry<T>(entry));
	}
	
	/**
	 * Writes a null entry to the cache.
	 * @param key the cache key
	 */
	@Override
	protected void addNullEntry(Object key) {
		if (key == null) return;
		RedisUtils.write(createKey(key), _expiryTime, new RemoteCacheEntry<T>(null));
	}
	
	/**
	 * Checks whether the cache contains a given key.
	 * @param key the cache key
	 * @return TRUE if the cache contains the key and is not expired, otherwsie FALSE
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object key) {
		String mcKey = createKey(key);
		try {
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) RedisUtils.get(mcKey);
			if ((e != null) && (e.getCreatedOn() < _lastFlushTime)) {
				RedisUtils.delete(mcKey);
				return false;
			} else if (e == null)
				return false;
			
			return true;
		} catch (Exception e) {
			_errors.increment();
			return false;
		}
	}

	/**
	 * Retrieves an object from the cache.
	 * @param key the Cache key
	 * @return the object, or null if not found 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T get(Object key) {
		request();
		String mcKey = createKey(key);
		try {
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) RedisUtils.get(mcKey);
			if (e == null)
				return null;
			else if (e.getCreatedOn() < _lastFlushTime)
				RedisUtils.delete(mcKey);
			
			T data = e.get();
			hit();
			return data;
		} catch (Exception e) {
			_errors.increment();
			return null;	
		}
	}
	
	/**
	 * Removes an object from the cache.
	 * @param key the cache key
	 */
	@Override
	public void remove(Object key) {
		try {
			RedisUtils.delete(createKey(key));
		} finally {
			// empty
		}
	}

	/**
	 * Clears the cache. This does not expire any memcached objects, but simply causes
	 * them to be discarded when fetched.
	 */
	@Override
	public void clear() {
		_lastFlushTime = System.currentTimeMillis();
	}
	
	/**
	 * This method is repurposed to deliver the round-trip latency to the cache server.
	 * @return the latency in nanoseconds
	 */
	@Override
	public int getMaxSize() {
		try {
			long startTime = System.nanoTime();
			RedisUtils.get(RedisUtils.LATENCY_KEY);
			return (int)(System.nanoTime() - startTime);
		} catch (Exception e) {
			_errors.increment();
			return 0;
		}
	}
}
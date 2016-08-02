// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.InvalidClassException;

import java.util.concurrent.atomic.LongAdder;

import org.deltava.util.MemcachedUtils;

/**
 * An object cache using memcached as its backing store.
 * @author Luke
 * @version 7.1
 * @since 6.1
 */

@Deprecated
public class MemcachedCache<T extends Cacheable> extends Cache<T> {
	
	private final String _bucket;
	private final int _expiryTime;
	private long _lastFlushTime;
	private final LongAdder _errors = new LongAdder();
	
	/**
	 * Creates a new Cache.
	 * @param bucket the mecmached bucket to use
	 * @param expiry the expiration time in seconds
	 */
	public MemcachedCache(String bucket, int expiry) {
		super(1);
		_bucket = bucket;
		_expiryTime = Math.min(3600 * 24 * 30, Math.max(1, expiry));
	}
	
	/*
	 * Creates a memcached bucket:key key.
	 */
	private String createKey(Object key) {
		StringBuilder buf = new StringBuilder(_bucket).append(':');
		return buf.append(String.valueOf(key)).toString();
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
		int expTime = _expiryTime;
		if (entry instanceof ExpiringCacheable) {
			ExpiringCacheable ec = (ExpiringCacheable) entry;
			expTime = (int) (ec.getExpiryDate().toEpochMilli() / 1000);
		}
		
		try {
			MemcachedUtils.write(createKey(entry.cacheKey()), expTime, new RemoteCacheEntry<T>(entry));
		} catch (Exception e) {
			// empty
		}
	}
	
	/**
	 * Writes a null entry to the cache.
	 * @param key the cache key
	 */
	@Override
	protected void addNullEntry(Object key) {
		if (key == null) return;
		try {
			MemcachedUtils.write(createKey(key), _expiryTime, new RemoteCacheEntry<T>(null));
		} catch (Exception e) {
			// empty
		}
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
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) MemcachedUtils.get(mcKey, 100);
			if ((e != null) && (e.getCreatedOn() < _lastFlushTime)) {
				MemcachedUtils.delete(mcKey);
				return false;
			} else if (e == null)
				return false;
			
			return true;
		} catch (InvalidClassException ice) {
			_errors.increment();
			MemcachedUtils.delete(mcKey);
			return false;
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
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) MemcachedUtils.get(mcKey, 125);
			if (e == null)
				return null;
			else if (e.getCreatedOn() < _lastFlushTime)
				MemcachedUtils.delete(mcKey);
			
			T data = e.get();
			hit();
			return data;
		} catch (InvalidClassException ice) {
			_errors.increment();
			MemcachedUtils.delete(mcKey);
			return null;
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
			MemcachedUtils.delete(createKey(key));
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
			MemcachedUtils.get(MemcachedUtils.LATENCY_KEY, 3500);
			return (int)(System.nanoTime() - startTime);
		} catch (Exception e) {
			_errors.increment();
			return 0;
		}
	}
}
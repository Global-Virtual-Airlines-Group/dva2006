// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Collection;

import org.deltava.util.MemcachedUtils;

/**
 * An object cache using memcached as its backing store.
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class MemcachedCache<T extends Cacheable> extends ExpiringCache<T> {
	
	private final String _bucket;
	private final int _expiryTime;
	
	/**
	 * Creates a new Cache.
	 * @param bucket the mecmached bucket to use
	 * @param maxSize the maximum size of the cache
	 * @param expiry the expiration time in seconds
	 */
	public MemcachedCache(String bucket, int maxSize, int expiry) {
		super(maxSize, expiry);
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
	 * Adds an entry to the cache.
	 * @param entry the Cacheable object
	 */
	@Override
	protected void addEntry(T entry) {
		if (entry == null) return;
		int expTime = _expiryTime;
		if (entry instanceof ExpiringCacheable) {
			ExpiringCacheable ec = (ExpiringCacheable) entry;
			expTime = (int) (ec.getExpiryDate().getTime() / 1000);
		}
		
		try {
			MemcachedUtils.write(createKey(entry.cacheKey()), expTime, entry);
			super.addNullEntry(entry.cacheKey());
		} catch (Exception e) {
			// empty
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
		try {
			CacheEntry<T> entry = _cache.get(key);
			if (entry == null) return null;
			
			Object o = MemcachedUtils.get(createKey(key), 125);
			if (o != null) 
				hit();
			else
				super.remove(key);
			
			return (T) o;
		} catch (Exception e) {
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
			super.remove(key);	
		}
	}
	
	/** 
	 * Clears the cache.
	 */
	@Override
	public void clear() {
		Collection<Object> keys = _cache.keySet();
		keys.forEach(k -> MemcachedUtils.delete(createKey(k)));
		super.clear();
	}
}
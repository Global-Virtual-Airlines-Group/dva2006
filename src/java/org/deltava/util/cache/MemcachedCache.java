// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.util.MemcachedUtils;

/**
 * An object cache using memcached as its backing store.
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class MemcachedCache<T extends Cacheable> extends Cache<T> {
	
	private final String _bucket;
	private final int _expiryTime;
	
	/*
	 * Null cache entry.
	 */
	private static class NullEntry implements java.io.Serializable {
		static final NullEntry INSTANCE = new NullEntry();
	}
	
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
		} catch (Exception e) {
			// empty
		}
	}

	/**
	 * Adds a null entry to the cache.
	 * @param key the cache key
	 */
	@Override
	protected void addNullEntry(Object key) {
		if (key == null) return;
		try {
			MemcachedUtils.write(createKey(key), _expiryTime, NullEntry.INSTANCE);
		} catch (Exception e) {
			// empty
		}
	}
	
	/**
	 * Checks whether the cache contains a given key. This will require a remote network call.
	 * @param key the cache key
	 * @return TRUE if the key exists in the cache, otherwise FALSE
	 */
	@Override
	public boolean contains(Object key) {
		request();
		try {
			Object o = MemcachedUtils.get(createKey(key), 100);
			if (o != null) hit();
			return (o != null);
		} catch (Exception e) {
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
		try {
			Object o = MemcachedUtils.get(createKey(key), 125);
			if (o != null) hit();
			return (o instanceof NullEntry) ? null : (T) o;
		} catch (Exception e) {
			return null;	
		}
	}
	
	/**
	 * Since memcached handles expiration, this is a no-op.
	 */
	@Override
	protected void checkOverflow() {
		// empty
	}
}
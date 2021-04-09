// Copyright 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

import org.deltava.util.RedisUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * An object cache using Redis as its backing store.
 * @author Luke
 * @version 10.0
 * @since 7.1
 * @param <T> the Cacheable object type
 */

public class RedisCache<T extends Cacheable> extends Cache<T> {
	
	private final String _bucket;
	private final int _expiryTime;
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
	
	@Override
	public long getErrors() {
		return _errors.longValue();
	}

	@Override
	protected void addEntry(T entry) {
		if (entry == null) return;
		RedisUtils.write(createKey(entry.cacheKey()), getExpiryTime(entry), new RemoteCacheEntry<T>(entry));
	}

	@Override
	public void addAll(Collection<? extends T> entries) {
		if ((entries == null) || entries.isEmpty()) return;
		try (Jedis jc = RedisUtils.getConnection()) {
			Pipeline jp = jc.pipelined();
			for (T entry : entries) {
				long expiry = getExpiryTime(entry);
				byte[] rawKey = RedisUtils.encodeKey(createKey(entry.cacheKey()));
				byte[] data = RedisUtils.write(new RemoteCacheEntry<T>(entry));
				long expTime = (expiry <= 864000) ? (expiry + (System.currentTimeMillis() / 1000)) : expiry;
				jc.set(rawKey, data);
				jc.expireAt(rawKey, expTime);
			}
			
			jp.sync();
		}
	}

	@Override
	protected void addNullEntry(Object key) {
		if (key == null) return;
		RedisUtils.write(createKey(key), _expiryTime, new RemoteCacheEntry<T>(null));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object key) {
		try {
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) RedisUtils.get(createKey(key));
			return (e != null);
		} catch (Exception e) {
			_errors.increment();
			return false;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(Object key) {
		request();
		try {
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) RedisUtils.get(createKey(key));
			if (e == null) return null;
			T data = e.get();
			hit();
			return data;
		} catch (Exception e) {
			_errors.increment();
			return null;	
		}
	}
	
	@Override
	public void remove(Object key) {
		try {
			RedisUtils.delete(createKey(key));
		} finally {
			// empty
		}
	}

	@Override
	public void clear() {
		Collection<String> keys = RedisUtils.keys(createKey("*"));
		RedisUtils.delete(keys.toArray(new String[0]));
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
	
	@Override
	public int size() {
		Collection<String> keys = RedisUtils.keys(createKey("*"));
		return keys.size();
	}
}
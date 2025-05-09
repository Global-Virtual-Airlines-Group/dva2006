// Copyright 2016, 2017, 2021, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.*;

import org.deltava.util.JedisUtils;

import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

/**
 * An object cache using ValKey as its backing store.
 * @author Luke
 * @version 11.6
 * @since 7.1
 * @param <T> the Cacheable object type
 */

public class JedisCache<T extends Cacheable> extends Cache<T> {
	
	private static Logger log = LogManager.getLogger(JedisCache.class);
	
	private final String _bucket;
	private final int _expiryTime;
	private final LongAdder _errors = new LongAdder();
	
	private long _lastSizeTime;
	private int _size;
	
	/**
	 * Creates a new Cache.
	 * @param bucket the cache key bucket to use
	 * @param expiry the expiration time in seconds
	 */
	public JedisCache(String bucket, int expiry) {
		super(1);
		_bucket = bucket;
		_expiryTime = Math.min(3600 * 24 * 30, Math.max(1, expiry));
	}
	
	/**
	 * Creates a Jedis bucket:key key.
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
		if (entry instanceof ExpiringCacheable ec) {
			Duration d = Duration.between(Instant.now(), ec.getExpiryDate());
			expTime = d.isNegative() ? 0 : d.toSeconds();
		}
		
		return expTime;
	}
	
	@Override
	public long getErrors() {
		return _errors.longValue();
	}
	
	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	protected void addEntry(T entry) {
		if (entry == null) return;
		JedisUtils.write(createKey(entry.cacheKey()), getExpiryTime(entry), new RemoteCacheEntry<T>(entry));
	}

	@Override
	public void addAll(Collection<? extends T> entries) {
		if ((entries == null) || entries.isEmpty()) return;
		try (Jedis jc = JedisUtils.getConnection()) {
			Pipeline jp = jc.pipelined();
			for (T entry : entries) {
				byte[] rawKey = JedisUtils.encodeKey(createKey(entry.cacheKey()));
				byte[] data = JedisUtils.write(new RemoteCacheEntry<T>(entry));
				jc.set(rawKey, data, SetParams.setParams().ex(getExpiryTime(entry)));
			}
			
			jp.sync();
		} catch (Exception e) {
			log.error("Cannot write to Jedis - {}", e.getMessage());
		}
	}

	@Override
	protected void addNullEntry(Object key) {
		if (key == null) return;
		JedisUtils.write(createKey(key), _expiryTime, new RemoteCacheEntry<T>(null));
	}
	
	@Override
	public boolean contains(Object key) {
		try {
			RemoteCacheEntry<?> e = (RemoteCacheEntry<?>) JedisUtils.get(createKey(key));
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
			RemoteCacheEntry<T> e = (RemoteCacheEntry<T>) JedisUtils.get(createKey(key));
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
		JedisUtils.delete(createKey(key));
	}

	@Override
	public void clear() {
		Collection<String> keys = JedisUtils.keys(createKey("*"));
		JedisUtils.delete(keys.toArray(new String[0]));
	}
	
	
	@Override
	public Map<Object, T> getAll(Collection<?> keys) {
		if (keys == null) return Collections.emptyMap();
		Map<Object, T> results = new HashMap<Object, T>();
		try (Jedis jc = JedisUtils.getConnection()) {
			for (Object k : keys) {
				request();
				String ek = createKey(k);
				try {
					byte[] rawValue = jc.get(JedisUtils.encodeKey(ek));
					if (rawValue != null) {
						@SuppressWarnings("unchecked")
						RemoteCacheEntry<T> ce = (RemoteCacheEntry<T>) JedisUtils.read(rawValue);
						if (ce != null) {
							results.put(k, ce.get());
							hit();
						}
					}
				} catch (Exception e) {
					log.warn("Error reading {} - {}", k, e.getMessage());
				}
			}
		} catch (Exception e2) {
			log.error("Cannot read from Jedis - {}", e2.getMessage());
		}
		
		return results;
	}
	
	/**
	 * This method is repurposed to deliver the round-trip latency to the cache server.
	 * @return the latency in nanoseconds
	 */
	@Override
	public int getMaxSize() {
		try {
			long startTime = System.nanoTime();
			JedisUtils.get(JedisUtils.LATENCY_KEY);
			return (int)(System.nanoTime() - startTime);
		} catch (Exception e) {
			_errors.increment();
			return 0;
		}
	}
	
	@Override
	public synchronized int size() {
		long now = System.currentTimeMillis();
		long sizeLatency = now - _lastSizeTime;
		if (sizeLatency > 600_000) {
			_lastSizeTime = now;
			Collection<String> keys = JedisUtils.keys(createKey("*"));
			_size = keys.size();
		}
		
		return _size;	
	}
}
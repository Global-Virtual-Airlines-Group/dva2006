// Copyright 2016, 2017, 2018, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;

import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

import org.gvagroup.pool.ConnectionPool;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 11.6
 * @since 6.1
 */

public class JedisUtils {
	
	private static final Logger log = LogManager.getLogger(JedisUtils.class);
	private static final List<String> INFO_KEYS = List.of("valkey_version", "redis_version", "uptime_in_seconds", "connected_clients", "used_memory", "maxmemory", "instantaneous_ops_per_sec", "db0");
	
	private static ConnectionPool<Jedis> _pool;
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	// static class
	private JedisUtils() {
		super();
	}
	
	/**
	 * Injects the Jedis connection pool.
	 * @param pool the pool
	 */
	public static void init(ConnectionPool<Jedis> pool) {
		_pool = pool;
	}
	
	/**
	 * Obtains a connection from the Jedis connection pool. <i>This exists for {@link org.deltava.util.cache.JedisCache}, and is ugly</i>
	 * @return a Jedis connection
	 * @throws Exception if the connection pool is full or not yet loaded
	 */
	public static Jedis getConnection() throws Exception {
		if (_pool == null)
			throw new IllegalStateException("Not Initialized");
		
		return _pool.getConnection();
	}
	
	/**
	 * Returns a key's encoding in UTF-8.
	 * @param key the key
	 * @return the key using the standard encoding
	 */
	public static byte[] encodeKey(String key) {
		return key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}
	
	/**
	 * Deserializes an object and swallows exceptions.
	 * @param data the raw data
	 * @return the deserialized data
	 */
	public static Object read(byte[] data) {
		try (ByteArrayInputStream bi = new ByteArrayInputStream(data); ObjectInputStream oi = new ObjectInputStream(bi)) {
			return oi.readObject();
		} catch (ClassNotFoundException cnfe) {
			log.warn("Cannot load {}", cnfe.getMessage());
		} catch (IOException ie) {
			// empty
		}
			
		return null;
	}
		
	/**
	 * Helper method to serialiaze an object and swallow checked exceptions.
	 * @param o the Object to serialize
	 * @return the serialized data
	 */
	public static byte[] write(Object o) {
		try (ByteArrayOutputStream bo = new ByteArrayOutputStream(1024); ObjectOutputStream oo = new ObjectOutputStream(bo)) {
			oo.writeObject(o);
			return bo.toByteArray();
		} catch (Exception e) {
			log.warn("Error writing {} - {}", o.getClass().getName(), e.getClass().getSimpleName());
		}
			
		return null;
	}
	
	/**
	 * Fetches an object from Redis.
	 * @param key the key
	 * @return the value
	 */
	public static Object get(String key) {
		byte[] rawKey = encodeKey(key);
		try (Jedis jc = getConnection()) {
			byte[] data = jc.get(rawKey);
			if (data == null) return null;
			Object o = read(data);
			if (o == null)
				jc.del(rawKey);
			
			return o;
		} catch (Exception e) {
			log.warn("Error reading cache {} - {}", key, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Writes an object to Redis.
	 * @param key the key
	 * @param expiry the expiry time in seconds from the present
	 * @param value the value
	 */
	public static void write(String key, long expiry, Object value) {
		if (value == null) return;
		byte[] rawKey = encodeKey(key);
		byte[] data = write(value);
		try (Jedis jc = getConnection()) {
			jc.set(rawKey, data, SetParams.setParams().ex(expiry));
		} catch (Exception e) {
			log.error("Error writing to Jedis - {}", e.getMessage());
		}
	}

	/**
	 * Pushes a value to a list. The list will be trimmed if it exceeds a maximum size.
	 * @param key the key
	 * @param value the list element
	 * @param maxLength the maximum size of the list or zero for unlimited
	 */
	public static void push(String key, String value, int maxLength) {
		try (Jedis jc = getConnection()) {
			long len = jc.rpush(key, value);
			if ((maxLength > 0) && (len > maxLength))
				jc.ltrim(key, (len - maxLength), len);
		} catch (Exception e) {
			log.error("Error writing to Jedis - {}", e.getMessage());
		}
	}

	/**
	 * Deletes keys from Redis.
	 * @param keys the keys
	 */
	public static void delete(String... keys) {
		try (Jedis jc = getConnection()) {
			Pipeline jp = jc.pipelined();
			for (String k : keys)
				jc.del(encodeKey(k));
			
			jp.sync();
		} catch (Exception e) {
			log.error("Error deleting from Jedis - {}", e.getMessage());
		}
	}
	
	/**
	 * Returns all keys matching a particular pattern.
	 * @param pattern the key pattern
	 * @return a Collection of keys
	 */
	public static Collection<String> keys(String pattern) {
		try (Jedis jc = getConnection()) {
			return jc.keys(pattern);
		} catch (Exception e) {
			log.warn("Error loading cache keys - {}", e.getMessage());
			return Collections.emptySet();
		}
	}
	
	/**
	 * Returns the Redis server and connection pool status.
	 * @return a Map of status attributes
	 */
	public static synchronized Map<String, Object> getStatus() {
		
		// Get server info - convert to int if possible
		Map<String, Object> results = new LinkedHashMap<String, Object>();
		try (Jedis jc = getConnection()) {
			List<String> inf = StringUtils.split(jc.info(), System.lineSeparator());
			for (String ie : inf) {
				int pos = ie.indexOf(':');
				if (pos > 0) {
					String k = ie.substring(0, pos); 
					if (INFO_KEYS.contains(k)) {
						String v = ie.substring(pos + 1); double nv = StringUtils.parse(v, Double.NaN);
						results.put(k, Double.isNaN(nv) ? v : Double.valueOf(nv));
					}
				}
			}
		} catch (Exception e) {
			log.warn("Cannot load Jedis status - {}", e.getMessage());
		}
		
		return results;
	}
}
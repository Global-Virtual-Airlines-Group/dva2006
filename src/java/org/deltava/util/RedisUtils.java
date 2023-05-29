// Copyright 2016, 2017, 2018, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;
import java.time.Duration;

import org.apache.logging.log4j.*;

import redis.clients.jedis.*;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 11.0
 * @since 6.1
 */

public class RedisUtils {
	
	private static final Logger log = LogManager.getLogger(RedisUtils.class);
	private static final List<String> INFO_KEYS = List.of("redis_version", "uptime_in_seconds", "connected_clients", "used_memory", "maxmemory", "instantaneous_ops_per_sec", "db0");
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	private static JedisPool _client;
	private static int _db;

	private static class DefaultJedisConfig implements JedisClientConfig {
		private DefaultJedisConfig() {
			super();
		}
	}
	
	// static class
	private RedisUtils() {
		super();
	}
	
	/*
	 * Checks the Redis connection.
	 */
	private static void checkConnection() {
		if (_client == null) throw new IllegalStateException("Not started");
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
	 * Helper method to deserialize an object and swallow exceptions.
	 * @param data the serialized data
	 * @return the Object
	 */
	public static Object read(byte[] data) {
		try (ByteArrayInputStream bi = new ByteArrayInputStream(data); ObjectInputStream oi = new ObjectInputStream(bi)) {
			return oi.readObject();
		} catch (ClassNotFoundException cnfe) {
			log.warn("Cannot load " + cnfe.getMessage());
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
			log.warn("Error writing " + o.getClass().getName() + " - " + e.getClass().getSimpleName());
		}
			
		return null;
	}
	
	/**
	 * Initializes the Redis connection.
	 * @param addr the address of the Redis server
	 * @param port the TCP/IP port number
	 * @param db the Redis database
	 * @param poolName the connection pool name
	 */
	public static synchronized void init(String addr, int port, int db, String poolName) {
		if (_client != null) return;
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(1); config.setMinIdle(1);
			config.setJmxEnabled(true);
			config.setJmxNamePrefix("redis-" + poolName.toLowerCase());
			config.setMaxWait(Duration.ofMillis(50));
			config.setMaxTotal(12);
			config.setSoftMinEvictableIdleTime(Duration.ofMillis(5000));
			config.setTestOnBorrow(false);
			config.setTestOnReturn(false);
			config.setTestWhileIdle(true);
			config.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
			
			// Check for domain socket
			String host = StringUtils.isEmpty(addr) ? "localhost" : addr;
			if (host.startsWith("/")) {
				log.info("Using Unix socket " + addr);
				JedisSocketFactory sf = new JedisDomainSocketFactory(host);
				_client = new JedisPool(config, sf, new DefaultJedisConfig());
			} else
				_client = new JedisPool(config, host, port);	
			
			_db = Math.max(0, db);
			write(LATENCY_KEY, 864000, String.valueOf((System.currentTimeMillis() / 1000) + (3600 * 24 * 365)));
			log.info("Initialized using database " + _db);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Terminates the Redis connection.
	 */
	public static synchronized void shutdown() {
		try {
			if (_client != null) {
				int isActive = _client.getNumActive();
				if (isActive > 0)
					log.warn(isActive + " active connections on close");

				log.info(_client.getNumIdle() + " idle connections on close");
				_client.close();
				_client.destroy();
				log.info("Disconnected");
			}
		} finally {
			_client = null;
		}
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
		}
	}
	
	/**
	 * Writes an object to Redis.
	 * @param key the key
	 * @param expiry the expiry time in seconds from the present or an absolute timestamp
	 * @param value the value
	 */
	public static void write(String key, long expiry, Object value) {
		if (value == null) return;
		byte[] rawKey = encodeKey(key);
		byte[] data = write(value);
		long expTime = (expiry <= 864000) ? (expiry + (System.currentTimeMillis() / 1000)) : expiry;
		try (Jedis jc = getConnection()) {
			Pipeline jp = jc.pipelined();
			jc.set(rawKey, data);
			jc.expireAt(rawKey, expTime);
			jp.sync();
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
		}
	}
	
	/**
	 * Retrieves a Redis connection from the pool.
	 * @return a Jedis client
	 */
	public static Jedis getConnection() {
		checkConnection();
		Jedis jc = _client.getResource();
		jc.select(_db);
		return jc;
	}
	
	/**
	 * Returns the Redis server and connection pool status.
	 * @return a Map of status attributes
	 */
	public static synchronized Map<String, Object> getStatus() {
		if (_client == null) return Collections.emptyMap();
		
		// Get server info - convert to int if possible
		Map<String, Object> results = new LinkedHashMap<String, Object>();
		try (Jedis jc = _client.getResource()) {
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
		}
		
		// Get connection pool info
		results.put("maxWait", Long.valueOf(_client.getMaxBorrowWaitTimeMillis()));
		results.put("meanWait", Long.valueOf(_client.getMeanBorrowWaitTimeMillis()));
		results.put("idle", Long.valueOf(_client.getNumIdle()));
		results.put("active", Long.valueOf(_client.getNumActive()));
		return results;
	}
}
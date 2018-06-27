// Copyright 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import redis.clients.jedis.*;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 8.3
 * @since 6.1
 */

public class RedisUtils {
	
	private static final Logger log = Logger.getLogger(RedisUtils.class);
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	/**
	 * The Jedis connection pool.
	 */
	protected static JedisPool _client;
	
	private static int _db;

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
	 * @param db the Redis database
	 * @param poolName the connection pool name
	 */
	public static synchronized void init(String addr, int db, String poolName) {
		if (_client != null) return;
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(1); config.setMinIdle(1);
			config.setJmxEnabled(true);
			config.setJmxNamePrefix("Redis");
			config.setJmxNameBase(poolName);
			config.setMaxWaitMillis(50);
			config.setMaxTotal(12);
			config.setMinEvictableIdleTimeMillis(5000);
			config.setTestOnBorrow(false);
			config.setTestOnReturn(false);
			config.setTestWhileIdle(true);
			config.setTimeBetweenEvictionRunsMillis(30000);
			_client = new JedisPool(config, addr, 6379);
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
		try (Jedis jc = getConnection()) {
			if (expiry > 864000) {
				jc.set(rawKey, data);
				jc.expireAt(rawKey, expiry);
			} else
				jc.setex(rawKey, (int) expiry, data);	
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
			long len = jc.rpush(key, value).longValue();
			if ((maxLength > 0) && (len > maxLength))
				jc.ltrim(key, (len - maxLength), len);
		}
	}

	/**
	 * Deletes a key from Redis.
	 * @param key the key
	 */
	public static void delete(String key) {
		try (Jedis jc = getConnection()) {
			jc.del(encodeKey(key));
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
	 * Returns the Redis connection pool status.
	 * @return a Map of status attributes
	 */
	public static synchronized Map<String, Long> getStatus() {
		if (_client == null) return Collections.emptyMap();
		Map<String, Long> results = new LinkedHashMap<String, Long>();
		results.put("maxWait", Long.valueOf(_client.getMaxBorrowWaitTimeMillis()));
		results.put("meanWait", Long.valueOf(_client.getMeanBorrowWaitTimeMillis()));
		results.put("idle", Long.valueOf(_client.getNumIdle()));
		results.put("active", Long.valueOf(_client.getNumActive()));
		return results;
	}
}
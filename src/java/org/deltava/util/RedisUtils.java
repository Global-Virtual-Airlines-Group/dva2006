// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import redis.clients.jedis.*;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 7.1
 * @since 6.1
 */

public class RedisUtils {
	
	private static final Logger log = Logger.getLogger(RedisUtils.class);
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	/**
	 * The Jedis onnection pool
	 */
	protected static JedisPool _client;

	/*
	 * Checks the memcached connection.
	 */
	private static void checkConnection() {
		if (_client == null) throw new IllegalStateException("Not started");
	}
	
	/**
	 * Initializes the Redis connection.
	 * @param addr the address of the Redis server
	 */
	public static synchronized void init(String addr) {
		if (_client != null) return;
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(1); config.setMinIdle(1);
			config.setMaxWaitMillis(75);
			config.setMaxTotal(12);
			config.setMinEvictableIdleTimeMillis(10000);
			_client = new JedisPool(config, addr, 6379);
			write(LATENCY_KEY, 864000, String.valueOf((System.currentTimeMillis() / 1000) + (3600 * 24 * 365)));
			log.warn("Initialized");
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
				_client.destroy();
				log.warn("Disconnected");
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
		checkConnection();
		byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
		try (Jedis jc = _client.getResource()) {
			byte[] data = jc.get(rawKey);
			if (data == null) return null;
			
			try (ByteArrayInputStream bi = new ByteArrayInputStream(data)) {
				try (ObjectInputStream oi = new ObjectInputStream(bi)) {
					return oi.readObject();
				}
			} catch (ClassNotFoundException cnfe) {
				log.warn("Cannot load " + cnfe.getMessage());
				jc.expire(rawKey, 0);
			} catch (IOException ie) {
				log.warn("Error reading " + key + " - " + ie.getMessage());
			}	
		}
		
		return null;
	}
	
	/**
	 * Writes an object to Redis.
	 * @param key the key
	 * @param expiry the expiry time in seconds from the present or an absolute timestamp
	 * @param value the value
	 */
	public static void write(String key, long expiry, Object value) {
		checkConnection();
		try (ByteArrayOutputStream bo = new ByteArrayOutputStream(1024)) {
			try (ObjectOutputStream oo = new ObjectOutputStream(bo)) {
				oo.writeObject(value);
			}

			byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
			try (Jedis jc = _client.getResource()) {
				if (expiry > 86400) {
					jc.set(rawKey, bo.toByteArray());
					jc.expireAt(rawKey, expiry);
				} else
					jc.setex(rawKey, (int) expiry, bo.toByteArray());	
			}
		} catch (IOException ie) {
			log.warn("Error writing to Redis - " + ie.getMessage());
		}
	}

	/**
	 * Pushes a value to a list. The list will be trimmed if it exceeds a maximum size.
	 * @param key the key
	 * @param value the list element
	 * @param maxLength the maximum size of the list or zero for unlimited
	 */
	public static void push(String key, String value, int maxLength) {
		checkConnection();
		try (Jedis jc = _client.getResource()) {
			long len = jc.rpush(key, value).longValue();
			if ((maxLength > 0) && (len > maxLength))
				jc.ltrim(key, (len - maxLength), len);
		}
	}

	/**
	 * Deletes a key from memcached.
	 * @param key the key
	 */
	public static void delete(String key) {
		checkConnection();
		try (Jedis jc = _client.getResource()) {
			jc.expire(key.getBytes(StandardCharsets.UTF_8), 0);
		}
	}
}
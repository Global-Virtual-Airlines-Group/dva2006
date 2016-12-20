// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import redis.clients.jedis.*;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 7.2
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
	 * Checks the Redis connection.
	 */
	private static void checkConnection() {
		if (_client == null) throw new IllegalStateException("Not started");
	}
	
	public static byte[] encodeKey(String key) {
		return key.getBytes(StandardCharsets.UTF_8);
	}
	
	public static Object read(byte[] data) {
		try (ByteArrayInputStream bi = new ByteArrayInputStream(data)) {
			try (ObjectInputStream oi = new ObjectInputStream(bi)) {
				return oi.readObject();
			}
		} catch (ClassNotFoundException cnfe) {
			log.warn("Cannot load " + cnfe.getMessage());
		} catch (IOException ie) {
			// empty
		}
			
		return null;
	}
		
	public static byte[] write(Object o) {
		try (ByteArrayOutputStream bo = new ByteArrayOutputStream(1024)) {
			try (ObjectOutputStream oo = new ObjectOutputStream(bo)) {
				oo.writeObject(o);
			}
				
			return bo.toByteArray();
		} catch (Exception e) {
			log.warn("Error writing " + o.getClass().getName() + " - " + e.getClass().getSimpleName());
		}
			
		return null;
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
			config.setTestWhileIdle(true);
			_client = new JedisPool(config, addr, 6379);
			write(LATENCY_KEY, 864000, String.valueOf((System.currentTimeMillis() / 1000) + (3600 * 24 * 365)));
			log.info("Initialized");
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
		checkConnection();
		byte[] rawKey = encodeKey(key);
		try (Jedis jc = _client.getResource()) {
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
		checkConnection();
		byte[] rawKey = encodeKey(key);
		byte[] data = write(value);
		try (Jedis jc = _client.getResource()) {
			if (expiry > 864000) {
				Pipeline p = jc.pipelined();
				p.set(rawKey, data);
				p.expireAt(rawKey, expiry);
				p.sync();
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
		checkConnection();
		try (Jedis jc = _client.getResource()) {
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
		checkConnection();
		try (Jedis jc = _client.getResource()) {
			jc.del(encodeKey(key));
		}
	}
	
	/**
	 * Retrieves a Redis connection from the pool.
	 * @return a Jedis client
	 */
	public static Jedis getConnection() {
		checkConnection();
		return _client.getResource();
	}
}
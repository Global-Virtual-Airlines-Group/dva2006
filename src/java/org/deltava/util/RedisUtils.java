// Copyright 2016, 2017, 2018, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;
import java.time.Duration;

import org.apache.commons.pool2.impl.*;
import org.apache.logging.log4j.*;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

/**
 * A utility class for Redis operations.
 * @author Luke
 * @version 11.3
 * @since 6.1
 */

public class RedisUtils {
	
	private static final Logger log = LogManager.getLogger(RedisUtils.class);
	private static final List<String> INFO_KEYS = List.of("redis_version", "uptime_in_seconds", "connected_clients", "used_memory", "maxmemory", "instantaneous_ops_per_sec", "db0");
	
	private static final int POOL_MAX_SIZE = 6;
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	private static JedisPool _client;
	private static int _db;
	private static String _poolName;

	private static class DefaultJedisConfig implements JedisClientConfig {
		private DefaultJedisConfig() {
			super();
		}
	}
	
	private static class PoolInfoComparator implements Comparator<DefaultPooledObjectInfo> {

		@Override
		public int compare(DefaultPooledObjectInfo o1, DefaultPooledObjectInfo o2) {
			int tmpResult = Long.compare(o1.getCreateTime(), o2.getCreateTime());
			return (tmpResult == 0) ? Long.compare(o1.getLastBorrowTime(), o2.getLastBorrowTime()) : tmpResult;
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
	 */
	private static Object read(byte[] data) {
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
			config.setJmxNamePrefix(String.format("redis-%s", poolName.toLowerCase()));
			config.setMaxWait(Duration.ofMillis(75));
			config.setMaxTotal(POOL_MAX_SIZE);
			config.setMinEvictableIdleDuration(Duration.ZERO);
			config.setSoftMinEvictableIdleDuration(Duration.ofMillis(5000));
			config.setNumTestsPerEvictionRun(config.getMaxTotal());
			config.setTestOnBorrow(false);
			config.setTestOnReturn(false);
			config.setTestWhileIdle(true);
			config.setTimeBetweenEvictionRuns(Duration.ofSeconds(60));
			config.setLifo(false);
			
			// Check for domain socket
			String host = StringUtils.isEmpty(addr) ? "localhost" : addr;
			if (host.startsWith("/")) {
				log.info("Using Unix socket {}", addr);
				JedisSocketFactory sf = new JedisDomainSocketFactory(host);
				_client = new JedisPool(config, sf, new DefaultJedisConfig());
			} else
				_client = new JedisPool(config, host, port);
			
			// Configure abandoned connection handling
			AbandonedConfig acfg = new AbandonedConfig();
			acfg.setLogAbandoned(true);
			acfg.setRemoveAbandonedOnBorrow(true);
			acfg.setRemoveAbandonedOnMaintenance(false);
			acfg.setRemoveAbandonedTimeout(Duration.ofMillis(3500));
			_client.setAbandonedConfig(acfg);
			
			_db = Math.max(0, db);
			_poolName = poolName;
			write(LATENCY_KEY, 864000, String.valueOf((System.currentTimeMillis() / 1000) + (3600 * 24 * 365)));
			log.info("{} initialized using database {}", _poolName, Integer.valueOf(_db));
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
					log.warn("{} active connections on close", Integer.valueOf(isActive));

				log.info("{} idle connections on close", Integer.valueOf(_client.getNumIdle()));
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
		} catch (JedisException je) {
			log.error("{} error writing to Jedis - {}", _poolName, je.getMessage());
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
		} catch (JedisException je) {
			log.error("{} error writing to Jedis - {}", _poolName, je.getMessage());
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
		} catch (JedisException je) {
			log.error("{} error deleting from Jedis - {}", je.getMessage());
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
		int poolSize = _client.getNumActive();
		jc.select(_db);
		jc.clientSetname(_poolName);
		if (poolSize >= POOL_MAX_SIZE)
			log.warn("{} pool size={}, max={}", _poolName, Integer.valueOf(poolSize), Integer.valueOf(POOL_MAX_SIZE));
			
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
		} catch (JedisException je) {
			log.warn("{} cannot load Jedis status - {}", _poolName, je.getMessage());
			return results;
		}
		
		// Get connection pool info
		results.put("useCount", Long.valueOf(_client.getBorrowedCount()));
		results.put("createCount", Long.valueOf(_client.getCreatedCount()));
		results.put("destroyCount", Long.valueOf(_client.getDestroyedCount()));
		results.put("maxWait", Long.valueOf(_client.getMaxBorrowWaitDuration().toMillis()));
		results.put("meanWait", Long.valueOf(_client.getMeanBorrowWaitDuration().toMillis()));
		results.put("idle", Long.valueOf(_client.getNumIdle()));
		results.put("active", Long.valueOf(_client.getNumActive()));
		return results;
	}

	/**
	 * Returns the status of all connection pool entries.
	 * @return a Collection of PoolConnectionInfo beans
	 */
	public static synchronized Collection<PoolConnectionInfo> getPoolStatus() {
		if (_client == null) return Collections.emptySet(); 
		
		Collection<DefaultPooledObjectInfo> data = CollectionUtils.sort(_client.listAllObjects(), new PoolInfoComparator());
		List<PoolConnectionInfo> results = new ArrayList<PoolConnectionInfo>(); int idx = 0;
		for (DefaultPooledObjectInfo inf : data)
			results.add(new PoolConnectionInfo(++idx, inf));
		
		return results; 
	}
}
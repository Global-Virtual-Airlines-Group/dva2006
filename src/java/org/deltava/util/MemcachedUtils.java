// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

import net.spy.memcached.*;

/**
 * A utility class for memcached operations.
 * @author Luke
 * @version 7.0
 * @since 6.1
 */

public class MemcachedUtils {
	
	private static final Logger log = Logger.getLogger(MemcachedUtils.class);
	
	/**
	 * Key used for round-trip latency tests.
	 */
	public static final String LATENCY_KEY = "$LATENCYTEST";
	
	/**
	 * The spymemcached client.
	 */
	protected static MemcachedClient _client;

	/*
	 * Checks the memcached connection.
	 */
	private static void checkConnection() {
		if (_client == null) throw new IllegalStateException("Not started");
	}
	
	/**
	 * Initializes the memcached connection.
	 * @param addrs a List of host:port addresses of the memcahed servers
	 */
	public static synchronized void init(List<String> addrs) {
		if (_client != null) return;
		try {
	        Properties systemProperties = System.getProperties();
	        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
	        System.setProperties(systemProperties);
			_client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(addrs));
			_client.set(LATENCY_KEY, (int)((System.currentTimeMillis() / 1000) + (3600 * 24 * 365)), Boolean.TRUE);
			log.warn("Initialized");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Terminates the memcached connection.
	 */
	public static synchronized void shutdown() {
		try {
			if (_client != null) {
				_client.shutdown();
				log.warn("Disconnected");
			}
		} finally {
			_client = null;
		}
	}
	
	/**
	 * Returns a memcached Compare and Set mutator.
	 * @return a CASMutator
	 */
	public static CASMutator<Object> getMutator() {
		return new CASMutator<Object>(_client, _client.getTranscoder());
	}
	
	/**
	 * Fetches an object from memcached.
	 * @param key the key
	 * @param ms the timeout in milliseconds
	 * @return the value
	 * @throws Exception if something bad happened, or a timeout
	 */
	public static Object get(String key, int ms) throws Exception {
		return get(key, ms, true);
	}
	
	/**
	 * Fetches an object from memcached.
	 * @param key the key
	 * @param ms the timeout in milliseconds
	 * @param failOnTimeout TRUE if an timeout should throw an exception, otherwise FALSE
	 * @return the value
	 * @throws Exception if something bad happened, or a timeout
	 */
	public static Object get(String key, int ms, boolean failOnTimeout) throws Exception {
		Future<Object> f = null;
		try {
			checkConnection();
			f = _client.asyncGet(key);
			return f.get(ms, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			f.cancel(true);
			if (failOnTimeout)
				return null;
			
			throw te;
		} catch (Exception e) {
			if (f != null) f.cancel(true);
			throw e;
		}
	}
	
	/**
	 * Writes an object to memcached.
	 * @param key the key
	 * @param expiry
	 * @param value the value
	 */
	public static void write(String key, int expiry, Object value) {
		checkConnection();
		_client.set(key, expiry, value);
	}

	/**
	 * Deletes a key from memcached.
	 * @param key the key
	 */
	public static void delete(String key) {
		checkConnection();
		_client.delete(key);
	}
}
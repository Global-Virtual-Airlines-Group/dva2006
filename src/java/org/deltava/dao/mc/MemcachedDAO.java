// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.Arrays;
import java.util.Properties;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

import net.spy.memcached.*;

/**
 * A Data Access Object to read and write from memcached. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

abstract class MemcachedDAO {
	
	/**
	 * The spymemcached client.
	 */
	protected static MemcachedClient _client;
	
	protected int _expiry;
	private String _bucket;

	/**
	 * Checks the memcached connection.
	 * @throws DAOException TRUE if the connection is not initialized
	 */
	protected static void checkConnection() throws DAOException {
		if (_client == null)
			init();
	}

	private static synchronized void init() throws DAOException {
		if (_client != null) return;
		try {
	        Properties systemProperties = System.getProperties();
	        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
	        System.setProperties(systemProperties);
			_client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost"));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Creates a memcached bucket:key key.
	 * @param key an object key
	 * @return a memcached key
	 */
	protected String createKey(Object key) {
		StringBuilder buf = new StringBuilder(_bucket).append(':');
		return buf.append(String.valueOf(key)).toString();
	}
	
	/**
	 * Sets the memcached bucket to store in. Bucket names will be chained together with a colon.
	 * @param buckets the bucket name(s)
	 */
	public void setBucket(String... buckets) {
		_bucket = StringUtils.listConcat(Arrays.asList(buckets), ":");
	}

	/**
	 * Sets the expiration date/time.
	 * @param ed
	 */
	public void setExpiry(int ed) {
		_expiry = ed;
	}
	
	/**
	 * Terminates the memcached connection.
	 */
	public static synchronized void shutdown() {
		try {
			if (_client != null)
				_client.shutdown();
		} finally {
			_client = null;
		}
	}
}
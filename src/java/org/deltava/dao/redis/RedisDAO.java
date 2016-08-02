// Copyright 2012, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

/**
 * A Data Access Object to read and write from Redis. 
 * @author Luke
 * @version 7.1
 * @since 5.0
 */

public abstract class RedisDAO {
	
	protected int _expiry;
	private String _bucket;

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
	public void setBucket(Object... buckets) {
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < buckets.length; x++) {
			Object b = buckets[x];
			if (b != null) {
				buf.append(String.valueOf(b));
				if (x < (buckets.length - 1))
					buf.append(':');
			}
		}
			
		_bucket = buf.toString();
	}

	/**
	 * Sets the expiration date/time.
	 * @param ed the number of seconds in the future to expire
	 */
	public void setExpiry(int ed) {
		_expiry = ed;
	}
}
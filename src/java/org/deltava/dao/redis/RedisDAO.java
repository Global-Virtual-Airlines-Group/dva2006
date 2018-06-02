// Copyright 2012, 2013, 2014, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

/**
 * A Data Access Object to read and write from Redis. 
 * @author Luke
 * @version 8.3
 * @since 5.0
 */

abstract class RedisDAO {
	
	protected int _expiry;
	private String _bucket;

	/**
	 * Creates a Redis bucket:key key.
	 * @param key an object key
	 * @return a Redis key
	 */
	protected String createKey(Object key) {
		StringBuilder buf = new StringBuilder(_bucket).append(':');
		return buf.append(String.valueOf(key)).toString();
	}
	
	/**
	 * Sets the Redis bucket to store in. Bucket names will be chained together with a colon.
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
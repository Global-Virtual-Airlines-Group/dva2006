// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A bean to store information about a cache.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class CacheInfo implements java.io.Serializable {
	
	private int _instances;
	private long _hits;
	private long _reqs;
	private long _size;
	private long _capacity;

	/**
	 * Initializes the information bean from a Cache.
	 * @param c the Cache 
	 */
	public CacheInfo(Cache<?> c) {
		super();
		add(c);
	}
	
	/**
	 * Adds another Cache's statistics to this bean.
	 * @param c the Cache
	 */
	public void add(Cache<?> c) {
		if (c != null) {
			_instances++;
			_hits += c.getHits();
			_reqs += c.getRequests();
			_size += c.size();
			_capacity += c.getMaxSize();
		}
	}
	
	/**
	 * Returns the number of cache instances used to generate these statistics.
	 * @return the number of caches;
	 */
	public int getInstances() {
		return _instances;
	}

	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public long getHits() {
		return _hits;
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public long getRequests() {
		return _reqs;
	}
	
	/**
	 * Returns the number of objects in the caches.
	 * @return the number of objects
	 */
	public long getSize() {
		return _size;
	}
	
	/**
	 * Returns the maximum size of the caches.
	 * @return the maximum number of objects
	 */
	public long getMaxSize() {
		return _capacity;
	}
	
	/**
	 * Dumps information about the CacheInfo bean.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder("cnt=");
		buf.append(_instances);
		buf.append(",hits=");
		buf.append(_hits);
		buf.append(",reqs=");
		buf.append(_reqs);
		buf.append(",size=");
		buf.append(_size);
		buf.append(",max=");
		buf.append(_capacity);
		return buf.toString();
	}
}
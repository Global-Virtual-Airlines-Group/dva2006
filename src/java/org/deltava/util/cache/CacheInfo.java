// Copyright 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A bean to store information about a cache.
 * @author Luke
 * @version 5.0
 * @since 2.6
 */

public class CacheInfo implements java.io.Serializable {

	private final String _id;
	private final String _type;
	
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
		this(null, c);
	}
	
	/**
	 * Initializes the information bean from a Cache.
	 * @param id the Cache ID
	 * @param c the Cache
	 */
	public CacheInfo(String id, Cache<?> c) {
		super();
		_id = id;
		_type = c.getClass().getSimpleName();
		add(c);
	}

	/**
	 * Adds another Cache's statistics to this bean.
	 * @param c the Cache
	 */
	public void add(Cache<?> c) {
		if (c == null) return;
		_instances++;
		_hits += c.getHits();
		_reqs += c.getRequests();
		_size += c.size();
		_capacity += c.getMaxSize();
	}

	/**
	 * Returns the number of cache instances used to generate these statistics.
	 * @return the number of caches;
	 */
	public int getInstances() {
		return _instances;
	}
	
	/**
	 * Returns the cache type.
	 * @return the type
	 */
	public String getType() {
		return _type;
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
	
	public int hashCode() {
		return (_id != null) ? _id.hashCode() : super.hashCode();
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
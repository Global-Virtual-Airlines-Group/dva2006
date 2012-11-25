// Copyright 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store information about a cache.
 * @author Luke
 * @version 5.0
 * @since 2.6
 */

public class CacheInfo implements java.io.Serializable, ViewEntry, Comparable<CacheInfo> {

	private final String _id;
	private final String _type;
	private final long _hits;
	private final long _reqs;
	private final long _size;
	private final long _capacity;

	/**
	 * Initializes the information bean from a Cache.
	 * @param id the Cache ID
	 * @param c the Cache
	 */
	CacheInfo(String id, Cache<?> c) {
		super();
		_id = id;
		_type = c.getClass().getSimpleName();
		_hits = c.getHits();
		_reqs = c.getRequests();
		_size = c.size();
		_capacity = c.getMaxSize();
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
	 * Returns the cache ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
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
	
	@Override
	public int hashCode() {
		return (_id != null) ? _id.hashCode() : super.hashCode();
	}
	
	public int compareTo(CacheInfo ci2) {
		int tmpResult = _id.compareTo(ci2._id);
		return (tmpResult == 0) ? Integer.valueOf(hashCode()).compareTo(Integer.valueOf(ci2.hashCode())) : tmpResult;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("id=");
		buf.append(_id);
		buf.append(",hits=").append(_hits);
		buf.append(",reqs=").append(_reqs);
		buf.append(",size=").append(_size);
		buf.append(",max=").append(_capacity);
		return buf.toString();
	}

	@Override
	public String getRowClassName() {
		return ("NullCache".equals(_type)) ? "warn" : null;
	}
}
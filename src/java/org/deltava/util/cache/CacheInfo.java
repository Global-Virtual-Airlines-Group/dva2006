// Copyright 2009, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store information about a cache.
 * @author Luke
 * @version 7.1
 * @since 2.6
 */

public class CacheInfo implements java.io.Serializable, ViewEntry, Comparable<CacheInfo> {

	private final String _id;
	private final String _type;
	private final long _hits;
	private final long _reqs;
	private final long _size;
	private final long _capacity;
	private final long _errors;
	private final boolean _isRemote;

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
		_isRemote = (c instanceof RedisCache);
		_errors = c.getErrors();
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
	 * Returns the number of cache errors. This is typically only used in remote caches.
	 * @return the number of errors
	 */
	public long getErrors() {
		return _errors;
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
	
	/**
	 * Returns whether this is a remote cache.
	 * @return TRUE if remote, otherwise FALSE
	 */
	public boolean getIsRemote() {
		return _isRemote;
	}
	
	@Override
	public int hashCode() {
		return (_id != null) ? _id.hashCode() : super.hashCode();
	}

	@Override
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
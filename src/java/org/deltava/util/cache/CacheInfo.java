// Copyright 2009, 2011, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A bean to store information about a cache.
 * @author Luke
 * @version 7.3
 * @since 2.6
 */

public class CacheInfo implements java.io.Serializable, org.deltava.beans.ViewEntry, Comparable<CacheInfo> {

	private final String _id;
	private final String _type;
	private final long _hits;
	private final long _reqs;
	private final long _size;
	private final long _capacity;
	private final long _errors;
	private final boolean _isRemote;
	private final boolean _isGeo;

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
		_isGeo = (c instanceof GeoCache);
		_errors = c.getErrors();
	}
	
	/**
	 * Initializes the information bean from a CacheInfo bean. This is used when combining cache info from multiple web applications.
	 * @param prefix the prefix to prepend in front of the Cache ID
	 * @param ci the CacheInfo bean
	 */
	public CacheInfo(String prefix, CacheInfo ci) {
		super();
		_id = prefix + "." + ci._id;
		_type = ci._type;
		_hits = ci._hits;
		_reqs = ci._reqs;
		_size = ci._size;
		_capacity = ci._capacity;
		_isRemote = ci._isRemote;
		_isGeo = ci._isGeo;
		_errors = ci._errors;
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
	
	/**
	 * Returns whether this is a geolocation cache.
	 * @return TRUE if geolocation, otherwise FALSE
	 */
	public boolean getIsGeo() {
		return _isGeo;
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
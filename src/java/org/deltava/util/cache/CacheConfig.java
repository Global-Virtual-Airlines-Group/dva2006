// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * A bean to store cache configuration when initializing caches. 
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

class CacheConfig {
	
	private final String _id;

	private int _maxSize;
	private int _expiryTime;
	
	private boolean _isRemote;
	
	private boolean _isGeo;
	private double _precision;
	
	/**
	 * Creates the configuration bean.
	 * @param id the cache ID
	 */
	CacheConfig(String id) {
		super();
		_id = id;
	}
	
	/**
	 * Returns whether this is a geolocation cache.
	 * @return TRUE if geolocation, otherwise FALSE
	 */
	public boolean isGeo() {
		return _isGeo;
	}
	
	/**
	 * Returns whether this is a remote cache.
	 * @return TRUE if remote, otherwise FALSE
	 */
	public boolean isRemote() {
		return _isRemote;
	}

	/**
	 * Returns the maximum size of the cache.
	 * @return the maximum size
	 */
	public int getMaxSize() {
		return _maxSize;
	}
	
	/**
	 * Returns the expiration time of the cache.
	 * @return the expiration time in seconds
	 */
	public int getExpiryTime() {
		return _expiryTime;
	}
	
	/**
	 * Returns the precision of a GeoLocation cache.
	 * @return the precision rounding factor
	 */
	public double getPrecision() {
		return _precision;
	}
	
	/**
	 * Returns the cache ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}

	/**
	 * Sets whether this is a GeoLocation cache.
	 * @param isGeo TRUE if geolocation, otherwise FALSE
	 */
	public void setGeo(boolean isGeo) {
		_isGeo = isGeo;
	}
	
	/**
	 * Sets whether this is a remote cache.
	 * @param isRemote TRUE if remote, otherwise FALSE
	 */
	public void setRemote(boolean isRemote) {
		_isRemote = isRemote;
	}
	
	/**
	 * Sets the maximum size of the cache.
	 * @param size the maximum number of cache entries
	 */
	public void setMaxSize(int size) {
		_maxSize = Math.max(0, size);
	}
	
	/**
	 * Sets the cache expiration time.
	 * @param time the expiration time in seconds
	 */
	public void setExpiryTime(int time) {
		_expiryTime = Math.max(-1, time);
	}
	
	/**
	 * Sets the preicsion of a GeoLocation cache.
	 * @param amt the rounding factor to apply
	 */
	public void setPrecision(double amt) {
		_precision = Math.max(0.00001, amt);
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CacheConfig) ? (hashCode() == o.hashCode()) : false;
	}
}
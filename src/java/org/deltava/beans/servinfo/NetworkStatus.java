// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store VATSIM-specific ServInfo properties.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NetworkStatus implements java.io.Serializable, Cacheable {

	private String _networkName;
	private Set _dataURLs;
	private String _msg;
	private boolean _isCached;

	/**
	 * Initializes the bean.
	 * @param networkName the network name
	 * @throws NullPointerException if network name is null
	 * @see NetworkStatus#getName()
	 */
	public NetworkStatus(String networkName) {
		super();
		_networkName = networkName.trim();
		_dataURLs = new HashSet();
	}

	/**
	 * Returns if this ServInfo data was cached by the DAO.
	 * @return TRUE if the data is cached, otherwise FALSE
	 */
	public boolean getCached() {
		return _isCached;
	}

	/**
	 * Returns the network name.
	 * @return the network name
	 * @see NetworkStatus#NetworkStatus(String)
	 */
	public String getName() {
		return _networkName;
	}

	/**
	 * Returns an available system message.
	 * @return the system message
	 * @see NetworkStatus#setMessage(String)
	 */
	public String getMessage() {
		return _msg;
	}

	/**
	 * Returns a random data location URL.
	 * @return the URL
	 * @see NetworkStatus#getURLs()
	 * @see NetworkStatus#addURL(String)
	 */
	public String getDataURL() {
		Random r = new Random();
		int idx = r.nextInt(_dataURLs.size());
		return (String) getURLs().get(idx);
	}

	/**
	 * Returns all data location URLs.
	 * @return a List of URLs.
	 * @see NetworkStatus#getDataURL()
	 * @see NetworkStatus#addURL(String)
	 */
	public List getURLs() {
		return new ArrayList(_dataURLs);
	}

	/**
	 * Sets the system message.
	 * @param msg the system message
	 * @see NetworkStatus#getMessage()
	 */
	public void setMessage(String msg) {
		_msg = msg;
	}

	/**
	 * Marks this data as cached.
	 * @see NetworkStatus#getCached()
	 */
	public void setCached() {
		_isCached = true;
	}

	/**
	 * Adds a data location URL.
	 * @param url the URL
	 * @see NetworkStatus#getDataURL()
	 * @see NetworkStatus#getURLs()
	 */
	public void addURL(String url) {
		_dataURLs.add(url);
	}

	/**
	 * Returns the network name's hash code.
	 */
	public int hashCode() {
		return _networkName.hashCode();
	}

	/**
	 * Returns the cache key for this object.
	 * @return the network name
	 */
	public Object cacheKey() {
		return _networkName;
	}
}
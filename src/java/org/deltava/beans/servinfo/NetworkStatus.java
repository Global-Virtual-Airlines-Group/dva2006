// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store VATSIM/IVAO-specific ServInfo properties.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NetworkStatus implements java.io.Serializable, Cacheable, Comparable {

	private String _networkName;
	private final List<NetworkDataURL> _dataURLs = new ArrayList<NetworkDataURL>();
	private NetworkDataURL _local;
	private String _msg;
	private boolean _isCached;

	/**
	 * Initializes the bean.
	 * @param networkName the network name
	 * @param localInfo the local information file or null
	 * @throws NullPointerException if network name is null
	 * @see NetworkStatus#NetworkStatus(String)
	 * @see NetworkStatus#getName()
	 * @see NetworkStatus#getLocal()
	 */
	public NetworkStatus(String networkName, String localInfo) {
		super();
		_networkName = networkName.trim();
		_local = (localInfo == null) ? null : new NetworkDataURL(localInfo);
	}
	
	/**
	 * Initializes the bean.
	 * @param networkName the network name
	 * @throws NullPointerException if network name is null
	 * @see NetworkStatus#NetworkStatus(String, String)
	 */
	public NetworkStatus(String networkName) {
		this(networkName, null);
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
	 * @see NetworkStatus#NetworkStatus(String, String)
	 */
	public String getName() {
		return _networkName;
	}
	
	/**
	 * Returns the URL to the local status file.
	 * @return the local status URL bean
	 */
	public NetworkDataURL getLocal() {
		return _local;
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
	 * Returns a data location URL.
	 * @param isRandom TRUE if a random location should be specified, otherwise the most reliable
	 * @return the URL
	 * @see NetworkStatus#getURLs()
	 * @see NetworkStatus#addURL(String)
	 */
	public NetworkDataURL getDataURL(boolean isRandom) {
		if (isRandom) {
			Random r = new Random();
			int idx = r.nextInt(_dataURLs.size());
			return _dataURLs.get(idx);
		}
	
		// Resort the collection and return
		return getURLs().get(_dataURLs.size() - 1);
	}
	
	/**
	 * Returns all data location URLs.
	 * @return a sorted List of URLs.
	 * @see NetworkStatus#getDataURL(boolean)
	 * @see NetworkStatus#addURL(String)
	 */
	@SuppressWarnings("unchecked")
	public List<NetworkDataURL> getURLs() {
		List<NetworkDataURL> results = new ArrayList<NetworkDataURL>(_dataURLs);
		Collections.sort(results);
		return results;
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
	 * Adds a data location URL. This checks to ensure that each URL is unique per network.
	 * @param url the URL
	 * @see NetworkStatus#getDataURL(boolean)
	 * @see NetworkStatus#getURLs()
	 */
	public void addURL(String url) {
		for (Iterator<NetworkDataURL> i = _dataURLs.iterator(); i.hasNext(); ) {
			NetworkDataURL nd = i.next();
			if (nd.getURL().equals(url))
				return;
		}
		
		// Add the URL if it's not unique
		_dataURLs.add(new NetworkDataURL(url));
	}

	/**
	 * Compares two networks by comparing their network names.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		NetworkStatus ns2 = (NetworkStatus) o;
		return _networkName.compareTo(ns2.getName());
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
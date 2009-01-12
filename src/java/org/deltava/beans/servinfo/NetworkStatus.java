// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store VATSIM/IVAO-specific ServInfo properties.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class NetworkStatus implements Cacheable, Comparable<NetworkStatus> {

	private OnlineNetwork _network;
	private final List<NetworkDataURL> _dataURLs = new ArrayList<NetworkDataURL>();
	private NetworkDataURL _local;
	private String _msg;

	/**
	 * Initializes the bean.
	 * @param network the network
	 * @param localInfo the local information file or null
	 * @see NetworkStatus#NetworkStatus(OnlineNetwork)
	 * @see NetworkStatus#getNetwork()
	 * @see NetworkStatus#getLocal()
	 */
	public NetworkStatus(OnlineNetwork network, String localInfo) {
		super();
		_network = network;
		_local = (localInfo == null) ? null : new NetworkDataURL(localInfo);
	}
	
	/**
	 * Initializes the bean.
	 * @param network the network
	 * @see NetworkStatus#NetworkStatus(OnlineNetwork, String)
	 */
	public NetworkStatus(OnlineNetwork network) {
		this(network, null);
	}

	/**
	 * Returns the network.
	 * @return the network
	 * @see NetworkStatus#NetworkStatus(OnlineNetwork)
	 * @see NetworkStatus#NetworkStatus(OnlineNetwork, String)
	 */
	public OnlineNetwork getNetwork() {
		return _network;
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
	 */
	public int compareTo(NetworkStatus ns2) {
		return _network.toString().compareTo(ns2._network.toString());
	}
	
	/**
	 * Returns the network name's hash code.
	 */
	public int hashCode() {
		return _network.hashCode();
	}

	/**
	 * Returns the cache key for this object.
	 * @return the network
	 */
	public Object cacheKey() {
		return _network;
	}
}
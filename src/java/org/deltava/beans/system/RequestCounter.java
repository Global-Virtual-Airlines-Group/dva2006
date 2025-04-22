// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

/**
 * A bean to track Spider requests.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RequestCounter implements java.io.Serializable {
	
	private final String _addr;
	private final List<Long> _reqs = new ArrayList<Long>();

	/**
	 * Initializes the counter.
	 * @param addr the remote address
	 */
	public RequestCounter(String addr) {
		super();
		_addr = addr;
	}

	/**
	 * Increments the request counter.
	 * @return the number of requests
	 */
	public synchronized int increment() {
		_reqs.add(Long.valueOf(System.currentTimeMillis()));
		return _reqs.size();
	}
	
	/**
	 * Returns the remote address.
	 * @return the address
	 */
	public String getAddress() {
		return _addr;
	}
	
	/**
	 * Returns the number of requests for this address.
	 * @return the number of requests
	 */
	public synchronized int getRequests() {
		return _reqs.size();
	}
	
	/**
	 * Purges all access times more than a certain period of time old.
	 * @param maxTime the time interval in seconds
	 */
	public synchronized void purge(int maxTime) {
		if (_reqs.isEmpty()) return;
		final long minTime = System.currentTimeMillis() - (maxTime * 1000);
		_reqs.removeIf(t -> t.longValue() < minTime);
	}
}
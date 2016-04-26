// Copyright 2005, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.util.cache.Cacheable;

/**
 * A class to store HTTP aggregate statistics.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class HTTPTotals implements Cacheable {
	
	private final int _totalHits;
	private final int _homeHits;
	private final long _totalBytes;

	/**
	 * Creates a new HTTP totals bean.
	 * @param totalHits the number of request
	 * @param homeHits the number of home page requests
	 * @param totalBandwidth the number of bytes served
	 */
	public HTTPTotals(int totalHits, int homeHits, long totalBandwidth) {
		super();
		_totalHits = totalHits;
		_homeHits = homeHits;
		_totalBytes = totalBandwidth;
	}

	/**
	 * Returns the total number of requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _totalHits;
	}
	
	/**
	 * Returns the total number of home page requests.
	 * @return the number of requests
	 */
	public int getHomeHits() {
		return _homeHits;
	}
	
	/**
	 * Returns the total bandwidth used.
	 * @return the number of bytes served.
	 */
	public long getBytes() {
		return _totalBytes;
	}
	
	/**
	 * Returns the cache key - the class.
	 */
	@Override
	public Object cacheKey() {
	   return getClass();
	}
}
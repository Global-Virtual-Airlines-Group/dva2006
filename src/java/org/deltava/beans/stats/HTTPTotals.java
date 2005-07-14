// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.stats;

import java.io.Serializable;

/**
 * A class to store HTTP aggregate statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HTTPTotals implements Serializable {
	
	private int _totalHits;
	private int _homeHits;
	private long _totalBytes;

	/**
	 * 
	 */
	public HTTPTotals(int totalHits, int homeHits, long totalBandwidth) {
		super();
		_totalHits = totalHits;
		_homeHits = homeHits;
		_totalBytes = totalBandwidth;
	}

	public int getHits() {
		return _totalHits;
	}
	
	public int getHomeHits() {
		return _homeHits;
	}
	
	public long getBytes() {
		return _totalBytes;
	}
}
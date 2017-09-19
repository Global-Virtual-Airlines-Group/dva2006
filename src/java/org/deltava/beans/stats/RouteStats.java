// Copyright 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.schedule.*;

/**
 * A bean to store route frequency data.
 * @author Luke
 * @version 8.0
 * @since 4.1
 */

public class RouteStats extends AbstractRoute implements Comparable<RouteStats> {
	
	public RouteStats(Airport aD, Airport aA, int freq) {
		super(aD, aA);
		_frequency = Math.max(0, freq);
	}

	/**
	 * Adds flights.
	 * @param cnt the number of flights
	 */
	public void add(int cnt) {
		_frequency += Math.max(0, cnt);
	}
	
	@Override
	public int compareTo(RouteStats rs2) {
		return Integer.compare(_frequency, rs2._frequency);
	}
}
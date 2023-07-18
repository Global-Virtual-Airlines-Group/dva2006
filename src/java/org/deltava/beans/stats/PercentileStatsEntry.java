// Copyright 2020, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;

/**
 * A bean to store flight legs/hours per year by percentile. 
 * @author Luke
 * @version 11.0
 * @since 9.2
 */

public class PercentileStatsEntry extends LegHoursDateStatsEntry<Integer> {
	
	private int _total;
	private final int _granularity;
	
	/**
	 * Creates the bean.
	 * @param startDate the starting date
	 * @param granularity the granularity of percentiles
	 */
	public PercentileStatsEntry(Instant startDate, int granularity) {
		super(startDate);
		_granularity = granularity;
	}
	
	private int adjust(int rawPct) {
		int p2 = Math.max(0, Math.min(100, rawPct));
		return p2 - (p2 % _granularity);
	}
	
	/**
	 * Returns the number of flight legs for a particular percentile.
	 * @param pctile the percentile
	 * @return the number of legs
	 */
	public int getLegs(int pctile) {
		return getLegs(Integer.valueOf(adjust(pctile)));
	}
	
	/**
	 * Returns the number of flight hours for a particular percentile.
	 * @param pctile the percentile
	 * @return the number of hours
	 */
	public int getDistance(int pctile) {
		return getDistance(Integer.valueOf(adjust(pctile)));
	}
	
	/**
	 * Returns the number of Elite points for a particular percentile.
	 * @param pctile the percentile
	 * @return the number of Elite points
	 */
	public int getPoints(int pctile) {
		return (int) getHours(Integer.valueOf(adjust(pctile)));
	}
	
	/**
	 * Returns the total number of Pilots.
	 * @return the number of Pilots
	 */
	public int getTotal() {
		return _total;
	}
	
	/**
	 * Sets percentile statistics.
	 * @param pctile the percentile
	 * @param legs the number of legs
	 * @param dst the distance in miles
	 * @param pts the Elite points
	 */
	public void setPercentile(int pctile, int legs, int dst, int pts) {
		set(Integer.valueOf(adjust(pctile)), legs, dst, pts);
	}

	/**
	 * Updates the total number of Pilots.
	 * @param cnt the number of Pilots
	 */
	public void setTotal(int cnt) {
		_total = cnt;
	}
}
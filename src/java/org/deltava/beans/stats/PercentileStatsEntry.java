// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;

/**
 * A bean to store flight legs/hours per year by percentile. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class PercentileStatsEntry extends LegHoursStatsEntry<Integer> {
	
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
		return (int)Math.round(getHours(Integer.valueOf(adjust(pctile))));
	}
	
	public int getTotal() {
		return _total;
	}
	
	/**
	 * Sets percentile statistics.
	 * @param pctile the percentile
	 * @param legs the number of legs
	 * @param dst the distance in miles
	 */
	public void setPercentile(int pctile, int legs, int dst) {
		set(Integer.valueOf(adjust(pctile)), legs, dst);
	}
	
	public void setTotal(int cnt) {
		_total = cnt;
	}
}
// Copyright 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.UsageFilter;

/**
 * A UsageFilter to filter runways based on popularity. 
 * @author Luke
 * @version 11.1
 * @since 10.2
 */

public class UsagePercentFilter implements UsageFilter<RunwayUse> {
	
	/**
	 * Filters all Runways.
	 */
	public static final UsagePercentFilter ALL = new UsagePercentFilter(0);
	
	private final int _minPercent;

	/**
	 * Creates the filter.
	 * @param minPct the minimum percentage of the maximum for inclusion
	 */
	public UsagePercentFilter(int minPct) {
		super();
		_minPercent = minPct;
	}
	
	@Override
	public boolean filter(RunwayUse ru, int max, int total) {
		ru.setPercentage((total == 0) ? 0 : ru.getUseCount() * 100 / total);
		if (max < 50) return true;
		
		int pctMax = ru.getUseCount() * 100 / max;
		return (pctMax >= _minPercent);
	}
}
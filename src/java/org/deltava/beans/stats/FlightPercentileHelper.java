// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;
import java.util.*;

import org.deltava.beans.Helper;
import org.deltava.beans.econ.YearlyTotal;

import org.deltava.comparators.YearlyTotalComparator;

/**
 * 
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

@Helper(PercentileStatsEntry.class)
public class FlightPercentileHelper {
	
	private final List<YearlyTotal> _totals = new ArrayList<YearlyTotal>();
	private final int _granularity;

	/**
	 * Creates the helper.
	 * @param totals a Collection of YearlyTotal beans by Pilot
	 * @param granularity the percentiile granularity 
	 */
	public FlightPercentileHelper(Collection<YearlyTotal> totals, int granularity) {
		super();
		_totals.addAll(totals);
		_granularity = granularity;
	}
	
	/**
	 * Sorts the raw results and breaks into percentiles based on the specified comparator.
	 */
	private PercentileStatsEntry generate(YearlyTotalComparator cmp) {
		List<YearlyTotal> totals = new ArrayList<YearlyTotal>(_totals);
		totals.sort(cmp);
		
		PercentileStatsEntry pse = new PercentileStatsEntry(Instant.now(), _granularity);
		pse.setTotal(totals.size());
		for (int pct = 0; pct < 100; pct += _granularity) {
			int idx = totals.size() * pct / 100;
			YearlyTotal yt = totals.get(idx);
			
			// Get the base of the percentile
			pse.setPercentile(pct, yt.getLegs(), yt.getDistance(), yt.getPoints());
		}
		
		return pse;
	}

	/**
	 * Returns the Flight Leg percentiles.
	 * @return a PercentileStatEentry keyed by flight leg
	 */
	public PercentileStatsEntry getLegs() {
		return generate(new YearlyTotalComparator(YearlyTotalComparator.LEGS));
	}
	
	/**
	 * Returns the Flight Distance percentiles.
	 * @return a PercentileStatEentry keyed by flight distance
	 */
	public PercentileStatsEntry getDistance() {
		return generate(new YearlyTotalComparator(YearlyTotalComparator.DISTANCE));
	}

	/**
	 * Returns the Elite Point percentiles.
	 * @return a PercentileStatEentry keyed by Elite point
	 */
	public PercentileStatsEntry getPoints() {
		return generate(new YearlyTotalComparator(YearlyTotalComparator.POINTS));
	}
}
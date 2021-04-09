// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.econ.*;

/**
 * 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class ElitePercentile implements EliteTotals, Comparable<ElitePercentile>, java.io.Serializable {
	
	private final int _pct;
	
	private int _legs;
	private int _distance;
	private int _pts;
	
	private final Map<EliteLevel, Integer> _levelPct = new TreeMap<EliteLevel, Integer>();
	
	/**
	 * Creates the bean.
	 * @param percentile the percentile
	 */
	public ElitePercentile(int percentile) {
		super();
		_pct = Math.min(100, Math.max(0, percentile));
	}

	public int getPercentile() {
		return _pct;
	}
	
	@Override
	public int getLegs() {
		return _legs;
	}
	
	@Override
	public int getDistance() {
		return _distance;
	}
	
	@Override
	public int getPoints() {
		return _pts;
	}
	
	public int getLevelPercentage(EliteLevel lvl) {
		return _levelPct.getOrDefault(lvl, Integer.valueOf(0)).intValue();
	}
	
	public void setInfo(int legs, int distance, int pts) {
		_legs = legs;
		_distance = distance;
		_pts = pts;
	}
	
	public void setLevelPercentage(EliteLevel lvl, int pct) {
		_levelPct.put(lvl, Integer.valueOf(pct));
	}

	@Override
	public int compareTo(ElitePercentile ep2) {
		return Integer.compare(_pct, ep2._pct);
	}
}
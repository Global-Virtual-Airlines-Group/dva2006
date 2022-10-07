// Copyright 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.Tuple;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store gate usage statistics.
 * @author Luke
 * @version 10.3
 * @since 10.0
 */

public class GateUsage implements Cacheable, RoutePair {
	
	private final Airport _aD;
	private final Airport _aA;
	private final boolean _isDeparture;
	private final int _dayRange;
	
	private final Map<String, Tuple<Integer, Integer>> _usage = new HashMap<String, Tuple<Integer, Integer>>();

	/**
	 * Creates the bean.
	 *  @param rp the RoutePair
	 *  @param isDeparture TRUE if departure gates, otherwise FALSE
	 *  @param daysBack the number of days usage depicted, or zero for all
	 */
	public GateUsage(RoutePair rp, boolean isDeparture, int daysBack) {
		super();
		_aD = rp.getAirportD();
		_aA = rp.getAirportA();
		_isDeparture = isDeparture;
		_dayRange = daysBack;
	}

	@Override
	public Airport getAirportD() {
		return _aD;
	}

	@Override
	public Airport getAirportA() {
		return _aA;
	}
	
	/**
	 * Returns the number of Gates with usage data.
	 * @return the number of Gates
	 */
	public int size() {
		return _usage.size();
	}
	
	/**
	 * Returns the number of Gates with recent usage data.
	 * @return the number of Gates
	 */
	public int getRecentSize() {
		return _usage.values().stream().filter(t -> t.getRight().intValue() > 0).mapToInt(e -> 1).sum(); // count that returns an int instead of a long
	}
	
	/**
	 * Returns the number of days usage depicted.
	 * @return the number of days, or zero for all
	 */
	public int getDayRange() {
		return _dayRange;
	}
	
	/**
	 * Returns whether these are departure gate statistics.
	 * @return TRUE if departure statistics, otherwise FALSE
	 */
	public boolean getIsDeparture() {
		return _isDeparture;
	}
	
	/**
	 * Adds usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @param totalUsage the total usage count
	 * @param recentUsage the recent usage count
	 */
	public void addGate(String gateName, int totalUsage, int recentUsage) {
		if (totalUsage > 0)
			_usage.put(gateName, Tuple.create(Integer.valueOf(totalUsage), Integer.valueOf(recentUsage)));
	}
	
	/**
	 * Returns the usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getTotalUsage(String gateName) {
		Tuple<Integer, Integer> t = _usage.getOrDefault(gateName, Tuple.create(Integer.valueOf(0), Integer.valueOf(0)));
		return t.getLeft().intValue();
	}
	
	/**
	 * Returns the usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getRecentUsage(String gateName) {
		Tuple<Integer, Integer> t = _usage.getOrDefault(gateName, Tuple.create(Integer.valueOf(0), Integer.valueOf(0)));
		return t.getRight().intValue();
	}
	
	/**
	 * Returns the total usage across all gates.
	 * @return the total usage count
	 */
	public int getTotal() {
		return _usage.values().stream().mapToInt(t -> t.getLeft().intValue()).sum();
	}
	
	/**
	 * Returns the total recent usage across all gates.
	 * @return the total recent usage count
	 */
	public int getTotalRecent() {
		return _usage.values().stream().mapToInt(t -> t.getRight().intValue()).sum();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}
	
	@Override
	public String toString() {
		return String.format("RT-%1s-%2s-%3b", (_aD == null) ? "null" : _aD.getICAO(), (_aA == null) ? "null" : _aA.getICAO(), Boolean.valueOf(_isDeparture));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
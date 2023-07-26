// Copyright 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Runway/Gate usage statistics.
 * @author Luke
 * @version 11.1
 * @since 10.0
 */

abstract class RunwayGateUsage implements Cacheable, RoutePair {
	
	private final Airport _aD;
	private final Airport _aA;
	private final boolean _isDeparture;
	
	/**
	 * The usage totals.
	 */
	protected final Collection<RunwayGateTotal> _usage = new TreeSet<RunwayGateTotal>();

	/**
	 * Creates the bean.
	 *  @param rp the RoutePair
	 *  @param isDeparture TRUE if departure gates/runways, otherwise FALSE
	 */
	public RunwayGateUsage(RoutePair rp, boolean isDeparture) {
		super();
		_aD = rp.getAirportD();
		_aA = rp.getAirportA();
		_isDeparture = isDeparture;
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
	 * Returns the number of Runways/Gates with usage data.
	 * @return the number of Runways/Gates
	 */
	public int size() {
		return _usage.size();
	}
	
	/**
	 * Returns whether these are departure Runway/Gate statistics.
	 * @return TRUE if departure statistics, otherwise FALSE
	 */
	public boolean getIsDeparture() {
		return _isDeparture;
	}
	
	/**
	 * Adds usage count for a particular Gate/Runway and Airline.
	 * @param name the Runway/Gate name
	 * @param a the Airline
	 * @param totalUsage the total usage count
	 */
	public void add(String name, Airline a, int totalUsage) {
		if (totalUsage > 0)
			_usage.add(new RunwayGateTotal(name, a, totalUsage));
	}
	
	/**
	 * Returns the usage count for a particular Gate/Runway.
	 * @param gateName the Runway/Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getTotalUsage(String gateName) {
		return _usage.stream().filter(gt -> gt.getName().equals(gateName)).mapToInt(RunwayGateTotal::getTotal).sum();
	}
	
	/**
	 * Returns the total usage across all gates.
	 * @return the total usage count
	 */
	public int getTotal() {
		return _usage.stream().mapToInt(RunwayGateTotal::getTotal).sum();
	}

	/**
	 * Clears usage totals.
	 */
	public void clear() {
		_usage.clear();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
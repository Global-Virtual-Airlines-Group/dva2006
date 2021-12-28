// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store gate usage statistics.
 * @author Luke
 * @version 10.2
 * @since 10.0
 */

public class GateUsage implements Cacheable, RoutePair {
	
	private final Airport _aD;
	private final Airport _aA;
	private final boolean _isDeparture;
	
	private final Map<String, Integer> _usage = new HashMap<String, Integer>();

	/**
	 * Creates the bean.
	 *  @param rp
	 *  @param isDeparture
	 */
	public GateUsage(RoutePair rp, boolean isDeparture) {
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
	 * Returns whether these are departure gate statistics.
	 * @return TRUE if departure statistics, otherwise FALSE
	 */
	public boolean getIsDeparture() {
		return _isDeparture;
	}
	
	/**
	 * Adds usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @param usage the usage count
	 */
	public void addGate(String gateName, int usage) {
		_usage.put(gateName, Integer.valueOf(usage));
	}
	
	/**
	 * Returns the usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getUsage(String gateName) {
		return _usage.getOrDefault(gateName, Integer.valueOf(0)).intValue();
	}
	
	/**
	 * Returns the total usage across all gates.
	 * @return the total usage count
	 */
	public int getTotal() {
		return _usage.values().stream().mapToInt(Integer::intValue).sum();
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
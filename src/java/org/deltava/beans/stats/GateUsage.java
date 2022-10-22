// Copyright 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.schedule.*;

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
	
	private final Collection<GateTotal> _usage = new TreeSet<GateTotal>();

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
		return _usage.stream().filter(gt -> (gt.getRecent() > 0)).mapToInt(e -> 1).sum(); // count that returns an int instead of a long
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
	 * Returns whether there is any Gate date for a particular Airline.
	 * @param airlineCode the Airline code
	 * @return TRUE if at least one Gate has been used with this Airline, otherwise FALSE
	 */
	public boolean hasAriline(String airlineCode) {
		return _usage.stream().anyMatch(gt -> airlineCode.equals(gt.getAirlineCode()));
	}
	
	/**
	 * Adds usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @param airlineCode the Airline code
	 * @param totalUsage the total usage count
	 * @param recentUsage the recent usage count
	 */
	public void addGate(String gateName, String airlineCode, int totalUsage, int recentUsage) {
		if (totalUsage > 0)
			_usage.add(new GateTotal(gateName, airlineCode, totalUsage, recentUsage));
	}
	
	/**
	 * Returns the usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getTotalUsage(String gateName) {
		return _usage.stream().filter(gt -> gt.getGateName().equals(gateName)).mapToInt(GateTotal::getTotal).sum();
	}
	
	/**
	 * Returns the usage count for a particular Gate.
	 * @param gateName the Gate name
	 * @return the usage count, or zero if unknown
	 */
	public int getRecentUsage(String gateName) {
		return _usage.stream().filter(gt -> gt.getGateName().equals(gateName)).mapToInt(GateTotal::getRecent).sum();
	}
	
	/**
	 * Returns the total usage across all gates.
	 * @return the total usage count
	 */
	public int getTotal() {
		return _usage.stream().mapToInt(GateTotal::getTotal).sum();
	}
	
	/**
	 * Returns the total recent usage across all gates.
	 * @return the total recent usage count
	 */
	public int getTotalRecent() {
		return _usage.stream().mapToInt(GateTotal::getRecent).sum();
	}
	
	/**
	 * Deep clones the object.
	 * @return a cloned GateUsage
	 */
	@Override
	public GateUsage clone() {
		GateUsage gu = new GateUsage(this, _isDeparture, _dayRange);
		_usage.stream().map(gt -> new GateTotal(gt.getGateName(), gt.getAirlineCode(), gt.getTotal(), gt.getRecent())).forEach(gu._usage::add);
		return gu;
	}
	
	/**
	 * Creates a GateUsage object for a single airline.
	 * @param airlineCode the Airline code
	 * @return a new GateUsage bean with statistics for that Airline
	 */
	public GateUsage filter(String airlineCode) {
		GateUsage gu = new GateUsage(this, _isDeparture, _dayRange);
		_usage.stream().filter(gt -> airlineCode.equals(gt.getAirlineCode())).forEach(gu._usage::add);
		return gu;
	}
	
	public void clear() {
		_usage.clear();
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
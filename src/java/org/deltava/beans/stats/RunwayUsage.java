// Copyright 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store Runway/Gate usage statistics.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class RunwayUsage extends RunwayGateUsage {
	
	/**
	 * Number of years back to aggregate runway usage.
	 */
	public static final int USAGE_YEARS = 4;
	
	/**
	 * Creates the bean.
	 *  @param rp the RoutePair
	 *  @param isDeparture TRUE if departure gates/runways, otherwise FALSE
	 */
	public RunwayUsage(RoutePair rp, boolean isDeparture) {
		super(rp, isDeparture);
	}
	
	/**
	 * Adds usage count for a particular Runway.
	 * @param name the Runway/Gate name
	 * @param totalUsage the total usage count
	 */
	public void add(String name, int totalUsage) {
		add(name, null, totalUsage);
	}

	/**
	 * Applys runway remapping to usage totals. Totals for the old and new codes will be added and applied to the new runway code.
	 * @param rm a RunwayMapping bean
	 */
	public void apply(RunwayMapping rm) {
		int ot = getTotalUsage(rm.getOldCode());
		int nt = getTotalUsage(rm.getOldCode());
		_usage.removeIf(rt -> rt.getName().equals(rm.getOldCode()) || rt.getName().equals(rm.getNewCode()));
		add(rm.getNewCode(), null, ot+nt);
	}
	
	/**
	 * Applies usage data to multiple Runways.
	 * @param rwys a Collection of Runway beans.
	 * @return a List of RunwayUse beans
	 */
	public List<RunwayUse> apply(Collection<Runway> rwys) {
		List<RunwayUse> results = rwys.stream().map(RunwayUse::new).collect(Collectors.toList());
		results.forEach(r -> r.setUseCount(getTotalUsage(r.getName()) + getTotalUsage(r.getAlternateCode())));
		return results;
	}
	
	/**
	 * Deep clones the object.
	 * @return a cloned RunwayUsage
	 */
	@Override
	public RunwayUsage clone() {
		RunwayUsage rgu = new RunwayUsage(this, getIsDeparture());
		_usage.stream().map(gt -> new RunwayGateTotal(gt.getName(), gt.getAirline(), gt.getTotal())).forEach(rgu._usage::add);
		return rgu;
	}
	
	@Override
	public String toString() {
		return String.format("RwyUse-%1s-%2s-%3b", (getAirportD()== null) ? "null" : getAirportD().getICAO(), (getAirportA() == null) ? "null" : getAirportA().getICAO(), Boolean.valueOf(getIsDeparture()));
	}
}
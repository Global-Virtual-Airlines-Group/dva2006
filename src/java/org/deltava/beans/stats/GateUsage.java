// Copyright 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

/**
 * A bean to store Runway/Gate usage statistics.
 * @author Luke
 * @version 11.1
 * @since 10.0
 */

public class GateUsage extends RunwayGateUsage {
	
	/**
	 * Number of years back to aggregate gate/airline usage.
	 */
	public static final int GATE_USAGE_YEARS = 6;
	
	/**
	 * Creates the bean.
	 *  @param rp the RoutePair
	 *  @param isDeparture TRUE if departure gates/runways, otherwise FALSE
	 */
	public GateUsage(RoutePair rp, boolean isDeparture) {
		super(rp, isDeparture);
	}
	
	/**
	 * Returns airlines with usage.
	 * @return a Collection of Airline codes
	 */
	public Collection<Airline> getAirlines() {
		return _usage.stream().map(RunwayGateTotal::getAirline).collect(Collectors.toSet());
	}
	
	/**
	 * Returns whether there is any Runway/Gate date for a particular Airline.
	 * @param airlineCode the Airline code
	 * @return TRUE if at least one Runway/Gate has been used with this Airline, otherwise FALSE
	 */
	public boolean hasAriline(String airlineCode) {
		return _usage.stream().anyMatch(gt -> airlineCode.equals(gt.getAirline().getCode()));
	}
	
	/**
	 * Deep clones the object.
	 * @return a cloned RunwayGateUsage
	 */
	@Override
	public GateUsage clone() {
		GateUsage rgu = new GateUsage(this, getIsDeparture());
		_usage.stream().map(gt -> new RunwayGateTotal(gt.getName(), gt.getAirline(), gt.getTotal())).forEach(rgu._usage::add);
		return rgu;
	}
	
	/**
	 * Creates a RunwayGateUsage object for a single Airline.
	 * @param a the Airline
	 * @return a new RunwayGateUsage bean with statistics for that Airline
	 */
	public GateUsage filter(Airline a) {
		GateUsage rgu = new GateUsage(this, getIsDeparture());
		_usage.stream().filter(gt -> a.equals(gt.getAirline())).forEach(rgu._usage::add);
		return rgu;
	}
	
	@Override
	public String toString() {
		return String.format("GateUse-%1s-%2s-%3b", (getAirportD() == null) ? "null" : getAirportD().getICAO(), (getAirportA() == null) ? "null" : getAirportA().getICAO(), Boolean.valueOf(getIsDeparture()));
	}
}
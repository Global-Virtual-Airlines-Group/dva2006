// Copyright 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.navdata.*;
import org.deltava.beans.wx.METAR;

import org.deltava.comparators.RunwayComparator;

/**
 * A helper class to suggest Runway assignments.
 * @author Luke
 * @version 11.5
 * @since 11.2
 */

public class RunwayHelper implements RoutePair{
	
	private final AircraftPolicyOptions _opts;
	private final Airport _aD;
	private final Airport _aA;
	
	private final List<RunwayUse> _rwysD = new ArrayList<RunwayUse>();
	private final List<RunwayUse> _rwysA = new ArrayList<RunwayUse>();
	
	private final Collection<TerminalRoute> _sids = new ArrayList<TerminalRoute>();
	private final Collection<TerminalRoute> _stars = new ArrayList<TerminalRoute>();
	
	private METAR _wxD;
	private METAR _wxA;
	
	private static class RunwayLengthComparator implements Comparator<Runway> {
		
		@Override
		public int compare(Runway r1, Runway r2) {
			return Integer.compare(r1.getLength(), r2.getLength());
		}
	}

	/**
	 * Creates the helper.
	 * @param rp the RoutePair
	 * @param ac an optional AircraftPolicyOptions for aircraft data
	 */
	public RunwayHelper(RoutePair rp, AircraftPolicyOptions ac) {
		super();
		_opts = ac;
		_aD = rp.getAirportD();
		_aA = rp.getAirportA();
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
	 * Returns the loaded SIDs.
	 * @return a Collection of TerminalRoutes
	 */
	public Collection<TerminalRoute> getSIDs() {
		return _sids;
	}

	/**
	 * Returns the loaded STARs.
	 * @return a Collection of TerminalRoutes
	 */
	public Collection<TerminalRoute> getSTARs() {
		return _stars;
	}

	/**
	 * Sets the weather at the Airports.
	 * @param mD the departure METAR, or null if none
	 * @param mA the arrival METAR, or null if none
	 */
	public void setMETAR(METAR mD, METAR mA) {
		_wxD = mD;
		_wxA = mA;
	}
	
	/**
	 * Adds valid SIDs for the departure Airport.
	 * @param sids a Collection of TerminalRoutes
	 */
	public void addSIDs(Collection<TerminalRoute> sids) {
		_sids.clear();
		_sids.addAll(sids);
	}
	
	/**
	 * Adds valid STARs for the arrival Airport.
	 * @param stars a Collection of TerminalRoutes
	 */
	public void addSTARs(Collection<TerminalRoute> stars) {
		_stars.clear();
		_stars.addAll(stars);
	}	

	/**
	 * Adds departure runway usage.
	 * @param ruD a Collection of departure RunwayUsage beeans
	 * @param ruA a Collection of arrival RunwayUsage beeans
	 */
	public void addRunways(Collection<RunwayUse> ruD, Collection<RunwayUse> ruA) {
		_rwysD.clear(); _rwysA.clear();
		_rwysD.addAll(ruD); _rwysA.addAll(ruA);
	}

	/**
	 * Filters and sorts appropriate Runways based on winds, terminal routes and popularity.
	 * @param isDeparture TRUE to return departure Runways, otherwise FALSE
	 * @return a sorted List of RunwayUse beans
	 */
	public List<RunwayUse> getRunways(boolean isDeparture) {
		
		// Get valid runways for terminal routes
		Collection<TerminalRoute> tRoutes = isDeparture ? _sids : _stars;
		Collection<String> tRwys = tRoutes.stream().map(TerminalRoute::getRunway).collect(Collectors.toSet());

		// Get runways based on terminal routes, and optionally filter based on minimum length
		Collection<RunwayUse> ru = isDeparture ? _rwysD : _rwysA;
		List<RunwayUse> rwys = ru.stream().filter(r -> filter(r, tRwys)).collect(Collectors.toList());
		if (rwys.isEmpty()) return rwys;
		if ((_opts != null) && !rwys.isEmpty()) { // if all runways are too short (ex. KSNA) just use the longest one(s)
			Airport a = isDeparture ? _aD : _aA;
			int minACLength = isDeparture ? _opts.getTakeoffRunwayLength() : _opts.getLandingRunwayLength();
			if (minACLength < a.getMaximumRunwayLength()) {
				Collections.sort(rwys, new RunwayLengthComparator());
				int maxRwyLength = rwys.getFirst().getLength();
				rwys.removeIf(r -> r.getLength() < maxRwyLength);
			} else
				rwys.removeIf(r -> r.getLength() < minACLength);
		}
			
		// Sort based on winds
		METAR m = isDeparture ? _wxD : _wxA;
		if ((m != null) && (m.getWindSpeed() > 0))
			rwys.sort(new RunwayComparator(m.getWindDirection(), m.getWindSpeed(), true));
		else
			Collections.sort(rwys);
		
		// Don't get runways with significantly low usage
		int maxUse = rwys.getFirst().getUseCount();
		if (maxUse > 40)
			rwys.removeIf(r -> r.getUseCount() < (maxUse / 3));

		return rwys;
	}
	
	/*
	 * SID/STAR runway filtering helper method.
	 */
	private static boolean filter(Runway r, Collection<String> rwyNames) {
		return rwyNames.isEmpty() || rwyNames.stream().anyMatch(sr -> r.matches(sr));
	}
}
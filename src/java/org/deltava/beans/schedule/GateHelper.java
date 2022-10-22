// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.flight.FlightType;
import org.deltava.beans.stats.GateUsage;

import org.deltava.comparators.GateComparator;

/**
 * A helper class to handle gate assignments. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

@Helper(Gate.class)
public class GateHelper {
	
	private final Comparator<Gate> CMP = new GateComparator(GateComparator.USAGE).reversed();
	
	private final RoutePair _rp;
	private final Airline _a;
	private final int _maxGates;
	private final boolean _doShuffle;
	
	private final Collection<Gate> _dGates = new HashSet<Gate>();
	private final Collection<Gate> _aGates = new HashSet<Gate>();
	
	private GateUsage _dUsage;
	private GateUsage _aUsage;

	/**
	 * Creates the bean.
	 * @param rp the RoutePair
	 * @param a the Airline
	 * @param max the maximum number of gates returned
	 * @param doShuffle TRUE to randomize gate choices, otherwise FALSE
	 */
	public GateHelper(RoutePair rp, Airline a, int max, boolean doShuffle) {
		super();
		_rp = rp;
		_a = a;
		_maxGates = Math.max(1, max);
		_doShuffle = doShuffle;
		_dUsage = new GateUsage(rp, true, 0);
		_aUsage = new GateUsage(rp, false, 0);
	}
	
	/**
	 * Creates the bean.
	 * @param f the Flight
	 * @param max the maximum number of gates returned
	 * @param doShuffle TRUE to randomize gate choices, otherwise FALSE
	 */
	public GateHelper(Flight f, int max, boolean doShuffle) {
		this(f, f.getAirline(), max, doShuffle);
	}
	
	/**
	 * Adds gates at the departure Airport.
	 * @param gates a Collection of Gates
	 * @param gu a GateUsage bean
	 */
	public void addDepartureGates(Collection<Gate> gates, GateUsage gu) {
		_dGates.addAll(gates);
		_dUsage = gu;
	}
	
	/**
	 * Adds gates at the arrival Airport.
	 * @param gates a Collection of Gates
	 * @param gu a GateUsage bean
	 */
	public void addArrivalGates(Collection<Gate> gates, GateUsage gu) {
		_aGates.addAll(gates);
		_aUsage = gu;
	}
	
	/**
	 * Clears the raw gate sets.
	 */
	public void clear() {
		_dGates.clear();
		_dUsage.clear();
		_aGates.clear();
		_aUsage.clear();
	}

	/**
	 * Returns the departure Gate zone.
	 * @return the GateZone
	 */
	public GateZone getDepartureZone() {
		return switch (_rp.getFlightType()) {
			case USPFI -> GateZone.USPFI;
			case INTERNATIONAL -> GateZone.INTERNATIONAL;
			default -> GateZone.DOMESTIC;
		};
	}

	/**
	 * Returns the arrival Gate zone.
	 * @return the GateZone
	 */
	public GateZone getArrivalZone() {
		return (_rp.getFlightType() == FlightType.INTERNATIONAL) ? GateZone.INTERNATIONAL : GateZone.DOMESTIC;
	}
	
	/**
	 * Returns departure Gates for this route.
	 * @return a List of Gates, sorted by popularity
	 */
	public List<Gate> getDepartureGates() {
		List<Gate> filteredGates = filter(_dGates, _a, getDepartureZone());
		if (filteredGates.isEmpty())
			filteredGates.addAll(_dGates);

		GateUsage gu =  _dUsage.hasAriline(_a.getCode()) ? _dUsage.filter(_a.getCode()) : _dUsage;
		boolean hasRecent = (gu.getRecentSize() > 0);
		filteredGates.forEach(g -> g.setUseCount(hasRecent ? gu.getRecentUsage(g.getName()) : gu.getTotalUsage(g.getName())));
		return sortSliceShuffle(filteredGates);
	}
	
	/**
	 * Returns arrival Gates for this route.
	 * @return a List of Gates, sorted by popularity
	 */
	public List<Gate> getArrivalGates() {
		List<Gate> filteredGates = filter(_aGates, _a, getArrivalZone());
		if (filteredGates.isEmpty())
			filteredGates.addAll(_aGates);
		
		GateUsage gu =  _aUsage.hasAriline(_a.getCode()) ? _aUsage.filter(_a.getCode()) : _aUsage;
		boolean hasRecent = (gu.getRecentSize() > 0);
		filteredGates.forEach(g -> g.setUseCount(hasRecent ? gu.getRecentUsage(g.getName()) : gu.getTotalUsage(g.getName())));
		return sortSliceShuffle(filteredGates);
	}
	
	private List<Gate> sortSliceShuffle(List<Gate> results) {
		Collections.sort(results, CMP);
		if (results.size() > _maxGates) results.removeAll(results.subList(_maxGates, results.size()));
		if (_doShuffle) Collections.shuffle(results);
		return results;
	}

	/*
	 * Helper method to filter Gates by Zone and Airline.
	 */
	private static List<Gate> filter(Collection<Gate> gates, Airline a, GateZone gz) {
		List<Gate> fdGates = gates.stream().filter(g -> g.hasAirline(a)).collect(Collectors.toList());
		List<Gate> iGates = fdGates.stream().filter(g -> (g.getZone() == gz)).collect(Collectors.toList());
		return iGates.isEmpty() ? fdGates : iGates;
	}
}
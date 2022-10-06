// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightType;
import org.deltava.beans.navdata.*;

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
	 */
	public void addDepartureGates(Collection<Gate> gates) {
		_dGates.addAll(gates);
	}
	
	/**
	 * Adds gates at the arrival Airport.
	 * @param gates a Collection of Gates
	 */
	public void addArrivalGates(Collection<Gate> gates) {
		_aGates.addAll(gates);
	}
	
	/**
	 * Clears the raw gate sets.
	 */
	public void clear() {
		_dGates.clear();
		_aGates.clear();
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
		return sortSliceShuffle(filteredGates.isEmpty() ? new ArrayList<Gate>(_dGates) : filteredGates);
	}
	
	/**
	 * Returns arrival Gates for this route.
	 * @return a List of Gates, sorted by popularity
	 */
	public List<Gate> getArrivalGates() {
		List<Gate> filteredGates = filter(_aGates, _a, getArrivalZone());
		return sortSliceShuffle(filteredGates.isEmpty() ? new ArrayList<Gate>(_aGates) : filteredGates);
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
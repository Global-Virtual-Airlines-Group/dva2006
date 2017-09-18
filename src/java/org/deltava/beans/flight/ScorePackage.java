// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.acars.ACARSRouteEntry;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.schedule.Aircraft;

/**
 * A bean to store data used to generate a Flight Score. 
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class ScorePackage {
	
	private final Aircraft _ac;
	private final FDRFlightReport _pirep;
	private final Runway _rD;
	private final Runway _rA;
	
	private final Collection<ACARSRouteEntry> _data = new ArrayList<ACARSRouteEntry>();
	
	/**
	 * Creates a Flight Scoring package.
	 * @param a the Aircraft used
	 * @param fr the FDRFlightReport
	 * @param rD the departure Runway
	 * @param rA the arrival Runway
	 */
	public ScorePackage(Aircraft a, FDRFlightReport fr, Runway rD, Runway rA) {
		super();
		_ac = a;
		_pirep = fr;
		_rD = rD;
		_rA = rA;
	}
	
	/**
	 * Adds data to the scoring package.
	 * @param entries a Collection of ACARSRouteEntry beans
	 */
	public void addData(Collection<ACARSRouteEntry> entries) {
		_data.addAll(entries);
	}
	
	/**
	 * Returns the aircraft profile used.
	 * @return the Aircraft
	 */
	public Aircraft getAircraft() {
		return _ac;
	}
	
	/**
	 * Returns the Flight Report.
	 * @return an FDRFlightReport
	 */
	public FDRFlightReport getFlightReport() {
		return _pirep;
	}
	
	/**
	 * Returns the flight data.
	 * @return a Collection of ACARSRouteEntry beans
	 */
	public Collection<ACARSRouteEntry> getData() {
		return _data;
	}
	
	/**
	 * Returns the departure Runway.
	 * @return a Runway
	 */
	public Runway getRunwayD() {
		return _rD;
	}

	/**
	 * Returns the arrival Runway.
	 * @return a Runway
	 */
	public Runway getRunwayA() {
		return _rA;
	}
	
	/**
	 * Returns all warnings from this data set.
	 * @return a Collection of Warnings
	 */
	public Collection<Warning> getWarnings() {
		return _data.stream().map(ACARSRouteEntry::getWarnings).flatMap(Collection::stream).collect(Collectors.toSet());
	}
}
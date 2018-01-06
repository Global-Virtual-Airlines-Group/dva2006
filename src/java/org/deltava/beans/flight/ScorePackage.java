// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.acars.ACARSRouteEntry;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.schedule.Aircraft;

/**
 * A bean to store data used to generate a Flight Score. 
 * @author Luke
 * @version 8.1
 * @since 8.0
 */

public class ScorePackage {
	
	private final Aircraft _ac;
	private final FDRFlightReport _pirep;
	private final Runway _rD;
	private final Runway _rA;
	
	private final Collection<ACARSRouteEntry> _data = new ArrayList<ACARSRouteEntry>();
	
	private FlightScore _result = FlightScore.INCOMPLETE;
	
	/**
	 * Comparator to sort warnings by severity in addition to ordinal.
	 */
	static class WarningComparator implements Comparator<Warning> {

		@Override
		public int compare(Warning w0, Warning w1) {
			
			int tmpResult = w0.getScore().compareTo(w1.getScore());
			return (tmpResult == 0) ? w0.compareTo(w1) : tmpResult;
		}
	}
	
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
	 * @param e an ACARSRouteEntry
	 */
	public void add(ACARSRouteEntry e) {
		_data.add(e);
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
		Collection<Warning> results = new TreeSet<Warning>(new WarningComparator());
		_data.stream().map(ACARSRouteEntry::getWarnings).flatMap(Collection::stream).filter(w -> w.getScore() != FlightScore.OPTIMAL).forEach(results::add);
		return results;
	}
	
	/**
	 * Returns the flight's score.
	 * @return the FlightScore
	 */
	public FlightScore getResult() {
		return _result;
	}
	
	/**
	 * Updates the flight's score.
	 * @param fs the FlightScore
	 */
	void setResult(FlightScore fs) {
		_result = fs;
	}
}
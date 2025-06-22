// Copyright 2017, 2018, 2019, 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.*;

/**
 * A bean to store data used to generate a Flight Score. 
 * @author Luke
 * @version 12.0
 * @since 8.0
 */

public class ScorePackage {
	
	private final Aircraft _ac;
	private final AircraftPolicyOptions _opts;
	private final FDRFlightReport _pirep;
	private final Runway _rD;
	private final Runway _rA;
	private Gate _gD;
	private Gate _gA;
	
	private final Collection<ACARSRouteEntry> _data = new ArrayList<ACARSRouteEntry>();
	
	private FlightScore _result = FlightScore.INCOMPLETE;
	
	private final Collection<String> _msgs = new LinkedHashSet<String>();
	
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
	 * @param opts the AircraftPolicyOptions to use
	 */
	public ScorePackage(Aircraft a, FDRFlightReport fr, Runway rD, Runway rA, AircraftPolicyOptions opts) {
		super();
		_ac = a;
		_pirep = fr;
		_rD = rD;
		_rA = rA;
		_opts = opts;
	}
	
	/**
	 * Adds data to the scoring package.
	 * @param re an ACARSRouteEntry
	 */
	public void add(RouteEntry re) {
		if (re instanceof ACARSRouteEntry are)
			_data.add(are);
	}
	
	/**
	 * Adds a message to the scoring package.
	 * @param msg the message
	 */
	public void add(String msg) {
		_msgs.add(msg);
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
	 * Returns the departure Gate.
	 * @return a Gate, or null if none
	 */
	public Gate getGateD() {
		return _gD;
	}
	
	/**
	 * Returns the arrival Gate.
	 * @return a Gate, or null if none
	 */
	public Gate getGateA() {
		return _gA;
	}
	
	/**
	 * Returns the aircraft policy options.
	 * @return an AircraftPolicyOptions bean
	 */
	public AircraftPolicyOptions getOptions() {
		return _opts;
	}
	
	/**
	 * Returns the scoring package messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;
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
	 * Returns the flight score.
	 * @return the FlightScore
	 */
	public FlightScore getResult() {
		return _result;
	}
	
	/**
	 * Updates the flight score.
	 * @param fs the FlightScore
	 */
	void setResult(FlightScore fs) {
		_result = fs;
	}
	
	/**
	 * Updates the departure and arrival Gates.
	 * @param gD the departure Gate, or null if none
	 * @param gA the arrival Gate, or null if none
	 */
	public void setGates(Gate gD, Gate gA) {
		_gD = gD;
		_gA = gA;
	}
}
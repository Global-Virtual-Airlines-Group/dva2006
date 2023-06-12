// Copyright 2012, 2016, 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.schedule.*;

/**
 * A bean to store route frequency data.
 * @author Luke
 * @version 11.0
 * @since 4.1
 */

public class RouteStats extends AbstractRoute implements Comparable<RouteStats> {
	
	private int _acarsFlights;
	private Instant _lastFlight;

	/**
	 * Creates the bean.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param freq the number of flights
	 */
	public RouteStats(Airport aD, Airport aA, int freq) {
		super(aD, aA);
		_frequency = Math.max(0, freq);
	}

	/**
	 * Adds flights.
	 * @param flights the total number of flights
	 * @param acarsFlights the number of ACARS flights
	 */
	public void add(int flights, int acarsFlights) {
		_acarsFlights += Math.max(0, acarsFlights);
		_frequency += Math.max(acarsFlights, flights);
	}
	
	/**
	 * Updates the date/time of the last flight.
	 * @param dt the date/time of the latest flight, or null if none
	 */
	public void setLastFlight(Instant dt) {
		_lastFlight = dt; 
	}
	
	/**
	 * Returns the number of ACARS flight legs. 
	 * @return the number of ACARS legs
	 */
	public int getACARSFlights() {
		return _acarsFlights;
	}
	
	/**
	 * Returns the last flight date for this route pair.
	 * @return the last flight date/time, or null if none
	 */
	public Instant getLastFlight() {
		return _lastFlight;
	}
	
	@Override
	public int compareTo(RouteStats rs2) {
		return Integer.compare(_frequency, rs2._frequency);
	}
}
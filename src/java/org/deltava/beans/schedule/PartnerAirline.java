// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store codeshare flight number data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PartnerAirline {

	private Airline _a;
	private int _start;
	private int _end;
	
	/**
	 * Populates the bean.
	 * @param a the Airline bean
	 * @param startFlight the first flight number served by this Airline
	 * @param endFlight the first flight number served by this Airline
	 */
	public PartnerAirline(Airline a, int startFlight, int endFlight) {
		super();
		_a = a;
		_start = startFlight;
		_end = endFlight;
	}

	/**
	 * Returns wether a flight number maps to this Airline.
	 * @param flightNumber the flight number
	 * @return TRUE if the Airline serves this flight, otherwise FALSE
	 */
	public boolean contains(int flightNumber) {
		return ((flightNumber >= _start) && (flightNumber <= _end));
	}
	
	/**
	 * Returns the Airline bean.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
}
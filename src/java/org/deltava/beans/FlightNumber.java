// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import org.deltava.beans.schedule.Airline;

/**
 * An interface to describe Flight Numbers. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public interface FlightNumber {

	/**
	 * Returns the Airline.
	 * @return the Airline
	 */
	public Airline getAirline();
	
	/**
	 * Returns the Flight Number.
	 * @return the flight number
	 */
	public int getFlightNumber();
	
	/**
	 * Returns the Flight Leg.
	 * @return the leg
	 */
	public int getLeg();
	
	/**
	 * Compares two Flight Numbers by comparing Airline, Number and Leg.
	 * @param f1 a FlightNumber
	 * @param f2 a FlightNumber
	 * @return -1, 0 or 1
	 */
	static int compare(FlightNumber f1, FlightNumber f2) {
		return compare(f1, f2, true);
	}
	
	/**
	 * Compares two Flight Numbers by comparing Airline, Number and optionally Leg.
	 * @param f1 a FlightNumber
	 * @param f2 a FlightNumber
	 * @param compareLegs TRUE if legs should be compared, otherwise FALSE
	 * @return -1, 0 or 1
	 */
	static int compare(FlightNumber f1, FlightNumber f2, boolean compareLegs) {
		int tmpResult = f1.getAirline().compareTo(f2.getAirline());
		if (tmpResult == 0)
			tmpResult = Integer.compare(f1.getFlightNumber(),  f2.getFlightNumber());
		
		if ((tmpResult == 0) && compareLegs)
			tmpResult = Integer.compare(f1.getLeg(),  f2.getLeg());
		
		return tmpResult;
	}
}
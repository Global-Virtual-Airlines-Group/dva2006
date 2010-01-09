// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.Date;

import org.deltava.beans.schedule.Airline;

/**
 * A class to store draft Flight Report data, with scheduled departure/arrival times. 
 * @author Luke
 * @version 2.8
 * @since 2.8
 */

public class DraftFlightReport extends FlightReport {
	
	private Date _timeD;
	private Date _timeA;

	/**
	 * Creates a new Flight Report object with a given flight.
	 * @param a the Airline
	 * @param flightNumber the Flight Number
	 * @param leg the Leg Number
	 * @throws NullPointerException if the Airline Code is null
	 * @throws IllegalArgumentException if the Flight Report is zero or negative
	 * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
	 */
	public DraftFlightReport(Airline a, int flightNumber, int leg) {
		super(a, flightNumber, leg);
	}

	/**
	 * Returns the scheduled departure time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the departure date/time
	 */
	public Date getTimeD() {
		return _timeD;
	}
	
	/**
	 * Returns the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the arrival date/time
	 */
	public Date getTimeA() {
		return _timeA;
	}
	
	/**
	 * Updates the scheduled departure time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the departure date/time
	 */
	public void setTimeD(Date dt) {
		_timeD = dt;
	}
	
	/**
	 * Updates the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the arrival date/time
	 */
	public void setTimeA(Date dt) {
		_timeA = dt;
	}
}
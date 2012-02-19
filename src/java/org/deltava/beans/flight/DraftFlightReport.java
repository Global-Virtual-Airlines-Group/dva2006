// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.Date;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.util.StringUtils;

/**
 * A class to store draft Flight Report data, with scheduled departure/arrival times. 
 * @author Luke
 * @version 4.1
 * @since 2.8
 */

public class DraftFlightReport extends FlightReport {
	
	private DateTime _timeD;
	private DateTime _timeA;
	
	/**
	 * Creates a new Flight Report object with a given Flight.
	 * @param f the Flight bean
	 */
	public DraftFlightReport(Flight f) {
		super(f);
	}

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
		return (_timeD == null) ? null : _timeD.getDate();
	}
	
	/**
	 * Returns the departure time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full departure time of the flight
	 * @see ScheduleEntry#getDateTimeA()
	 */
	public DateTime getDateTimeD() {
		return _timeD;
	}

	/**
	 * Returns the arrival time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full arrival time of the flight
	 * @see ScheduleEntry#getDateTimeD()
	 */
	public DateTime getDateTimeA() {
		return _timeA;
	}
	
	/**
	 * Returns the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the arrival date/time
	 */
	public Date getTimeA() {
		return (_timeA == null) ? null : _timeA.getDate();
	}
	
	/**
	 * Updates the scheduled departure time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the departure date/time
	 */
	public void setTimeD(Date dt) {
		TZInfo tz = (getAirportD() == null) ? TZInfo.local() : getAirportD().getTZ();
		_timeD = new DateTime(dt, tz);
	}
	
	/**
	 * Updates the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the arrival date/time
	 */
	public void setTimeA(Date dt) {
		TZInfo tz = (getAirportD() == null) ? TZInfo.local() : getAirportA().getTZ();
		_timeA = new DateTime(dt, tz);
	}
	
	/**
	 * Returns default comments with departure and arrival times.
	 */
	public String getDraftComments() {
		StringBuilder buf = new StringBuilder("Scheduled departure at ");
		buf.append(StringUtils.format(getTimeD(), "HH:mm"));
		buf.append(' ');
		buf.append(getAirportD().getTZ());
		buf.append(", scheduled arrival at ");
		buf.append(StringUtils.format(getTimeA(), "HH:mm"));
		buf.append(' ');
		buf.append(getAirportA().getTZ());
		return buf.toString();
	}
}
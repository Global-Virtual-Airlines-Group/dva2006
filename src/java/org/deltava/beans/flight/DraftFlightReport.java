// Copyright 2010, 2012, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;

/**
 * A class to store draft Flight Report data, with scheduled departure/arrival times. 
 * @author Luke
 * @version 8.1
 * @since 2.8
 */

public class DraftFlightReport extends FlightReport implements FlightTimes {
	
	private ZonedDateTime _timeD;
	private ZonedDateTime _timeA;
	
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
	@Override
	public ZonedDateTime getTimeD() {
		return _timeD;
	}
	
	/**
	 * Returns the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the arrival date/time
	 */
	@Override
	public ZonedDateTime getTimeA() {
		return _timeA;
	}
	
	/**
	 * Updates the scheduled departure time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the departure date/time
	 */
	public void setTimeD(LocalDateTime dt) {
		_timeD = ZonedDateTime.of(dt, getAirportD().getTZ().getZone());
	}
	
	/**
	 * Updates the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @param dt the arrival date/time
	 */
	public void setTimeA(LocalDateTime dt) {
		_timeA = ZonedDateTime.of(dt, getAirportA().getTZ().getZone());
	}
	
	/**
	 * Returns default comments with departure and arrival times.
	 * @return the comments
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
	
	@Override
	public Duration getDuration() {
		Duration d = Duration.between(_timeD.toInstant(), _timeA.toInstant());
		return d.isNegative() ? d.negated() : d;
	}
}
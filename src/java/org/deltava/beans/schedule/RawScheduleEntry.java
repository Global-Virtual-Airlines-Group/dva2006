// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.DayOfWeek;

import org.deltava.beans.Flight;

import org.deltava.util.StringUtils;

/**
 * A Schedule Entry with code share and day of week data. 
 * @author Luke
 * @version 8.3
 * @since 8.0
 */

public class RawScheduleEntry extends ScheduleEntry {

	private String _codeShare;
	private DayOfWeek _day;
	
	/**
	 * Creates the bean.
	 * @param a the Airline
	 * @param fNumber the flight number
	 * @param leg the leg number
	 */
	public RawScheduleEntry(Airline a, int fNumber, int leg) {
		super(a, fNumber, leg);
	}
	
	/**
	 * Creates the bean from an existing Flight Number.
	 * @param f a Flight
	 */
	public RawScheduleEntry(Flight f) {
		super(f.getAirline(), f.getFlightNumber(), f.getLeg());
	}

	/**
	 * If a codeshare, the flight code of the operator's flight.
	 * @return the flight code, or null if none
	 */
	public String getCodeShare() {
		return _codeShare;
	}
	
	/**
	 * Returns the day of the week this flight is operated on.
	 * @return a DayOfWeek enum
	 */
	public DayOfWeek getDay() {
		return _day;
	}

	/**
	 * Sets the day of the week that this flight is operated on.
	 * @param d a DayOfWeek enum
	 */
	public void setDay(DayOfWeek d) {
		_day = d;
	}
	
	/**
	 * If this is a codeshare flight, the flight code of the operator's flight.
	 * @param flightCode the flight code
	 */
	public void setCodeShare(String flightCode) {
		if (!StringUtils.isEmpty(flightCode))
			_codeShare = flightCode;
	}
}
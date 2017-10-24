// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.DayOfWeek;

import org.deltava.util.StringUtils;

/**
 * A Schedule Entry with code share and day of week data. 
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class RawScheduleEntry extends ScheduleEntry {

	private String _codeShare;
	private final Collection<DayOfWeek> _days = new TreeSet<DayOfWeek>();
	
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
	 * If a codeshare, the flight code of the operator's flight.
	 * @return the flight code, or null if none
	 */
	public String getCodeShare() {
		return _codeShare;
	}
	
	/**
	 * Returns the days of the week this flight is operated on.
	 * @return a Collection of DayOfWeek enums
	 */
	public Collection<DayOfWeek> getDays() {
		return _days;
	}

	/**
	 * Adds a day of the week that this flight is operated on.
	 * @param d a DayOfWeek enum
	 */
	public void addDay(DayOfWeek d) {
		_days.add(d);
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
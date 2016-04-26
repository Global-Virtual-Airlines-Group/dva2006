// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.ZonedDateTime;

/**
 * An interface to describe objects with departure and arrival times.
 * @author Luke
 * @version 7.0
 * @since 6.1
 */

public interface FlightTimes {
	
	/**
	 * Returns the departure time of the flight, with full timezone information. The date component of this value can be ignored.
	 * @return the full departure time of the flight
	 * @see FlightTimes#getTimeA()
	 */
	public ZonedDateTime getTimeD();

	/**
	 * Returns the arrival time of the flight, with full timezone information. The date component of this value can be ignored.
	 * @return the full arrival time of the flight
	 * @see FlightTimes#getTimeD()
	 */
	public ZonedDateTime getTimeA();
}
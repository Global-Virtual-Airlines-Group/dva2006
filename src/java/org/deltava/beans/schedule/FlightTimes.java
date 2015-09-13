// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

import org.deltava.beans.DateTime;

/**
 * An interface to describe objects with departure and arrival times.
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public interface FlightTimes {
	
	/**
	 * Returns the scheduled departure time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the departure date/time
	 */
	public Date getTimeD();

	/**
	 * Returns the departure time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full departure time of the flight
	 * @see FlightTimes#getDateTimeA()
	 */
	public DateTime getDateTimeD();

	/**
	 * Returns the arrival time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full arrival time of the flight
	 * @see FlightTimes#getDateTimeD()
	 */
	public DateTime getDateTimeA();
	
	/**
	 * Returns the scheduled arrival time <i>in local time</i>. The date portion
	 * should be ignored.
	 * @return the arrival date/time
	 */
	public Date getTimeA();
}
// Copyright 2015, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.ZonedDateTime;

/**
 * An interface to describe objects with departure and arrival times.
 * @author Luke
 * @version 10.1
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
	
	/**
	 * Returns if both the departure and arrrival date/times have been populated.
	 * @return TRUE if both times are populated, otherwise FALSE
	 * @see RoutePair#isPopulated()
	 */
	public default boolean hasTimes() {
		return (getTimeD() != null) && (getTimeA() != null);
	}
}
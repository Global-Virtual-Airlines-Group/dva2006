// Copyright 2012, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Duration;

/**
 * A bean to store Flight Time schedule search results.
 * @author Luke
 * @version 10.1
 * @since 4.2
 */

public class FlightTime implements java.io.Serializable {

	private final Duration _time;
	private final RoutePairType _type;
	
	/**
	 * Creates the bean.
	 * @param d the flight time as a Duration
	 * @param type a RoutePairType
	 */
	public FlightTime(Duration d, RoutePairType type) {
		super();
		_time = (d == null) || d.isNegative() ? Duration.ZERO : d;
		_type = type;
	}
	
	/**
	 * Returns the flight time.
	 * @return the flight time in hours multiplied by 10
	 */
	public Duration getFlightTime() {
		return _time;
	}
	
	/**
	 * Returns the pair type
	 * @return a RoutePairType
	 */
	public RoutePairType getType() {
		return _type;
	}
}
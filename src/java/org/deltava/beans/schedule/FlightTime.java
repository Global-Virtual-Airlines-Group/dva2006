// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store Flight Time schedule search results.
 * @author Luke
 * @version 7.5
 * @since 4.2
 */

public class FlightTime implements java.io.Serializable {

	private final int _time;
	private final RoutePairType _type;
	
	/**
	 * Creates the bean.
	 * @param time the flight time in hours <i>multiplied by 10</i>
	 * @param type a RoutePairType
	 */
	public FlightTime(int time, RoutePairType type) {
		super();
		_time = Math.max(0, time);
		_type = type;
	}
	
	/**
	 * Returns the flight time.
	 * @return the flight time in hours multiplied by 10
	 */
	public int getFlightTime() {
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
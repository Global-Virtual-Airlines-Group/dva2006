// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A bean to store leg/hour/miles data. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

class LegHoursStats implements java.io.Serializable {

	private final int _legs;
	private final int _distance;
	private final double _hours;

	/**
	 * Creates the bean.
	 * @param legs the number of flight legs
	 * @param distance the flight distance in miles
	 * @param hours the number of flight hours  
	 */
	LegHoursStats(int legs, int distance, double hours) {
		super();
		_legs = legs;
		_distance = distance;
		_hours = hours;
	}

	/**
	 * Returns the number of flight legs.
	 * @return the number of legs
	 */
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Returns the flight distance.
	 * @return the distance in miles
	 */
	public int getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the number of flight hours.
	 * @return the number of hours
	 */
	public double getHours() {
		return _hours;
	}
}
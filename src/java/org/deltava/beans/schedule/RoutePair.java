// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An interface to mark Airport pairs. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public interface RoutePair {

	/**
	 * Returns the departure Airport.
	 * @return the departure Airport 
	 */
	public Airport getAirportD();

	/**
	 * Returns the arrival Airport.
	 * @return the arrival Airport 
	 */
	public Airport getAirportA();
	
	/**
	 * Returns the distance between the Airports.
	 * @return the distance in miles
	 */
	public int getDistance();
	
	/**
	 * Returns whether the route crosses a particular meridian.
	 * @param lng the longitude in degrees
	 * @return TRUE if it crosses the meridian, otherwise FALSE
	 */
	public boolean crosses(double lng);
}
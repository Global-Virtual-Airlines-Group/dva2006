// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.Airport;

/**
 * An interface to mark Route Plotting questions and question profiles.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public interface RoutePlot {

	/**
	 * Returns the departure Airport for this route.
	 * @return the departure Airport bean
	 */
	public Airport getAirportD();
	
	/**
	 * Returns the arrival Airport for this route.
	 * @return the arrival Airport bean
	 */
	public Airport getAirportA();

	/**
	 * Updates the departure Airport.
	 * @param a the departure Airport bean
	 */
	public void setAirportD(Airport a);
	
	/**
	 * Updates the arrival Airport.
	 * @param a the arrival Airport bean
	 */
	public void setAirportA(Airport a);
	
	/**
	 * Returns the mid-point between the two Airports.
	 * @return the midpoint
	 */
	public GeoLocation getMidPoint();
	
	/**
	 * Returns the distance between the two Airports.
	 * @return the distance in miles
	 */
	public int getDistance();
}
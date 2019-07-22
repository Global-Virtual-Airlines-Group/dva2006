// Copyright 2009, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An interface to mark Airport pairs. 
 * @author Luke
 * @version 8.6
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
	default int getDistance() {
		return isPopulated() ? getAirportD().distanceTo(getAirportA()) : -1;
	}

	/**
	 * Returns whether both airports are populated.
	 * @return TRUE if both Airports are set, otherwise FALSE 
	 */
	default boolean isPopulated() {
		return ((getAirportD() != null) && (getAirportA() != null));
	}
	
    /**
     * Returns whether this RoutePair matches a particular Route Pair.
     * @param rp a RoutePair
     * @return TRUE if the departure and arrival Airports are the same, otherwise FALSE
     */
    default boolean matches(RoutePair rp) {
    	return isPopulated() && getAirportD().equals(rp.getAirportD()) && getAirportA().equals(rp.getAirportA());
    }
}
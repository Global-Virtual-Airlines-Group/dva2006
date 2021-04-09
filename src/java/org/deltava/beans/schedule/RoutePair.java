// Copyright 2009, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.flight.FlightType;

/**
 * An interface to mark Airport pairs. 
 * @author Luke
 * @version 10.0
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
    
    /**
     * Returns whether this RoutePair includes a particular IATA or ICAO code.
     * @param code the ICAO/IATA code
     * @return TRUE if the code matches the departure or arrival airport, otherwise FALSE
     */
    @SuppressWarnings("unlikely-arg-type")
    default boolean includes(String code) {
    	return isPopulated() && (getAirportD().equals(code) || getAirportA().equals(code));
    }
    
    /**
     * Returns the flight type for customs/gate purposes.
     * @return a FlightType enumeration
     */
    default FlightType getFlightType() {
    	if (!isPopulated()) return FlightType.UNKNOWN;
    	if (getAirportD().getCountry().equals(getAirportA().getCountry())) return FlightType.DOMESTIC;
    	if (getAirportD().getHasPFI() && "US".equals(getAirportA().getCountry().getCode())) return FlightType.USPFI;
    	if (getAirportD().getIsSchengen() & getAirportA().getIsSchengen()) return FlightType.SCHENGEN;
    	return FlightType.INTERNATIONAL;
    }
    
    /**
     * Returns a key that describes the route pair.
     * @return the departure/arrival ICAO codes
     */
    default String createKey() {
		StringBuilder buf = new StringBuilder(getAirportD().getICAO());
		buf.append('-');
		buf.append(getAirportA().getICAO());
		return buf.toString();
    }
}
// Copyright 2009, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.flight.FlightType;

/**
 * An interface to mark Airport pairs. 
 * @author Luke
 * @version 12.0
 * @since 2.6
 */

public interface RoutePair {
	
	/**
	 * Helper class when all you need is a naked RoutePair.
	 */
	static class RoutePairImpl implements RoutePair {
		private final Airport _aD;
		private final Airport _aA;
		
		private RoutePairImpl(Airport aD, Airport aA) {
			super();
			_aD = aD;
			_aA = aA;
		}
		
		@Override
		public Airport getAirportD() {
			return _aD;
		}
		
		@Override
		public Airport getAirportA() {
			return _aA;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("[");
			if (_aD != null)
				buf.append(_aD.getICAO());
			buf.append('-');
			if (_aA != null)
				buf.append(_aA.getICAO());
			buf.append(']');
			return buf.toString();
		}
	}

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
	 * @return the distance in miles, or -1 if not populated
	 */
	default int getDistance() {
		return isPopulated() ? getAirportD().distanceTo(getAirportA()) : -1;
	}
	
	/**
	 * Returns the midpoint between the two Airports on a Great Circle route.
	 * @return the midpoint GeoLocation, or null if not populated
	 */
	default GeoLocation midPoint() {
		return isPopulated() ? new GeoPosition(getAirportD()).midPoint(getAirportA()) : null;
	}

	/**
	 * Returns whether both airports are populated.
	 * @return TRUE if both Airports are set, otherwise FALSE 
	 */
	default boolean isPopulated() {
		return ((getAirportD() != null) && (getAirportA() != null));
	}
	
	/**
	 * Returns the airports in this Route Pair. <i>This is only filled if both Airports are populated.</i>
	 * @return a List of Airports, or an empty list if not populated.
	 * @see RoutePair#isPopulated()
	 */
	default Collection<Airport> getAirports() {
		return isPopulated() ? List.of(getAirportD(), getAirportA()) : Collections.emptyList();
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
    	if (getAirportA().getHasPFI() && "US".equals(getAirportD().getCountry().getCode())) return FlightType.USPFI;
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
    
    /**
     * Creates a new naked RoutePair.
     * @param aD the deprature Airport
     * @param aA the arrival Airport
     * @return a RoutePair
     */
    public static RoutePair of(Airport aD, Airport aA) {
    	return new RoutePairImpl(aD, aA);
    }
}
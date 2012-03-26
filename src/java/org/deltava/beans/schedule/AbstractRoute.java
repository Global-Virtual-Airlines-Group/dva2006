// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.GeoUtils;

/**
 * A bean to store route frequency data.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public abstract class AbstractRoute implements RoutePair {
	
	private final Airport _aD;
	private final Airport _aA;
	
	private final String _code;
	protected int _frequency;
	
	protected AbstractRoute(Airport aD, Airport aA) {
		super();
		_aD = aD;
		_aA = aA;
		
		// Build code
		Collection<String> airports = new TreeSet<String>();
		airports.add(aD.getICAO());
		airports.add(aA.getICAO());
		_code = airports.toString();
	}

	@Override
	public Airport getAirportD() {
		return _aD;
	}

	@Override
	public Airport getAirportA() {
		return _aA;
	}
	
	/**
	 * Returns a list of route points for inclusion on a Google Map. If this route crosses the 
	 * International Date Line, internal Great Circle calculations are used to get around a
	 * Google Maps bug.
	 * @return a Collection of GeoLocations
	 */
	public Collection<? extends GeoLocation> getPoints() {
		return GeoUtils.greatCircle(_aD, _aA, 100);
	}

	@Override
	public int getDistance() {
		return GeoUtils.distance(_aD, _aA);
	}

	/**
	 * Returns the number of flights in this route pair.
	 * @return the number of flights
	 */
	public int getFlights() {
		return _frequency;
	}
	
	/**
	 * Returns the route pair's hash code.
	 * @see ScheduleRoute#toString()
	 */
	public int hashCode() {
		return _code.hashCode();
	}

	/**
	 * Returns the route pair.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(_aD.getICAO());
		buf.append('-');				
		buf.append(_aA.getICAO());
		return buf.toString();
	}
}
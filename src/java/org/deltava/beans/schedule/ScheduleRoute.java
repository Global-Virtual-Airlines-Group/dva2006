// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.GeoUtils;

/**
 * A bean to store route pair information.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ScheduleRoute implements RoutePair, Comparable<ScheduleRoute>, ViewEntry {
	
	private Airline _a;
	private Airport _aD;
	private Airport _aA;
	
	private String _code;
	
	private int _flights;
	private int _routes;
	
	/**
	 * Creates a new Route Pair.
	 * @param a the Airline serving this route
	 * @param ad the departure Airport bean
	 * @param aa the arrival Airport bean
	 */
	public ScheduleRoute(Airline a, Airport ad, Airport aa) {
		super();
		_a = a;
		_aD = ad;
		_aA = aa;
		
		// Build code
		Collection<String> airports = new TreeSet<String>();
		airports.add(ad.getICAO());
		airports.add(aa.getICAO());
		_code = airports.toString();
	}
	
	/**
	 * Returns the Airline serving this airport pair.
	 * @return the Airline bean
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the departure Airport.
	 * @return the Airport bean
	 * @see ScheduleRoute#getAirports()
	 */
	public Airport getAirportD() {
		return _aD;
	}

	/**
	 * Returns the arrival Airport.
	 * @return the Airport bean
	 * @see ScheduleRoute#getAirports()
	 */
	public Airport getAirportA() {
		return _aA;
	}
	
	/**
	 * Returns the sorted airports in this route pair. 
	 * @return a Collection of Airport beans
	 * @see ScheduleRoute#getAirportA()
	 * @see ScheduleRoute#getAirportD()
	 */
	public Collection<Airport> getAirports() {
		return Arrays.asList(_aD, _aA);
	}
	
	/**
	 * Returns a list of route points for inclusion on a Google Map. If this route crosses the 
	 * International Date Line, internal Great Circle calculations are used to get around a
	 * Google Maps bug.
	 * @return a Collection of GeoLocations
	 */
	public Collection<? extends GeoLocation> getPoints() {
		
		// Check if we cross the date line
		if (crosses(-179.9))
			return GeoUtils.greatCircle(_aD, _aA, 100);

		return getAirports();
	}
	
	/**
	 * Returns the number of flights between these two airports.
	 * @return the number of flights
	 * @see ScheduleRoute#setFlights(int)
	 * @see ScheduleRoute#getRoutes()
	 */
	public int getFlights() {
		return _flights;
	}

	/**
	 * Returns the number of Dispatch rotues between these two airports.
	 * @return the number of routes
	 * @see ScheduleRoute#setRoutes(int)
	 * @see ScheduleRoute#getFlights()
	 */
	public int getRoutes() {
		return _routes;
	}
	
	/**
	 * Returns the distance between the airports.
	 */
	public int getDistance() {
		return GeoUtils.distance(_aD, _aA);
	}
	
	/**
     * Returns whether this route crosses a particular meridian.
     */
    public boolean crosses(double lng) {
    	return GeoUtils.crossesMeridian(_aD, _aA, lng);
    }

	/**
	 * Updates the number of flights between these two airports. 
	 * @param count  the number of flights
	 * @see ScheduleRoute#getFlights()
	 * @see ScheduleRoute#setRoutes(int)
	 */
	public void setFlights(int count) {
		_flights = Math.max(0, count);
	}
	
	/**
	 * Updates the number of Dispatch routes between these two airports.
	 * @param count the number of routes
	 * @see ScheduleRoute#getRoutes()
	 * @see ScheduleRoute#setFlights(int)
	 */
	public void setRoutes(int count) {
		_routes = Math.max(0, count);
	}
	
	public String getRowClassName() {
		return (_routes == 0) ? "opt1" : null;
	}
	
	/**
	 * Compares two route pairs by comparing their sorted airport codes.
	 * @see ScheduleRoute#toString()
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(ScheduleRoute rp2) {
		return _code.compareTo(rp2._code);
	}
	
	/**
	 * Compars two route pairs by comparing their airport codes.
	 * @see ScheduleRoute#toString()
	 */
	public boolean equals(Object o) {
		return (o instanceof ScheduleRoute) ? (compareTo((ScheduleRoute) o) == 0) : false;
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
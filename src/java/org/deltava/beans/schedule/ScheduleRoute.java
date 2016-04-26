// Copyright 2006, 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store route pair information.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ScheduleRoute extends AbstractRoute implements Comparable<ScheduleRoute>, ViewEntry {
	
	private final Airline _a;
	private int _routes;
	
	/**
	 * Creates a new Route Pair.
	 * @param ad the departure Airport bean
	 * @param aa the arrival Airport bean
	 */
	public ScheduleRoute(Airport ad, Airport aa) {
		this(null, ad, aa);
	}
	
	/**
	 * Creates a new Route Pair.
	 * @param a the Airline serving this route
	 * @param ad the departure Airport bean
	 * @param aa the arrival Airport bean
	 */
	public ScheduleRoute(Airline a, Airport ad, Airport aa) {
		super(ad, aa);
		_a = a;
	}
	
	/**
	 * Returns the Airline serving this airport pair.
	 * @return the Airline bean
	 */
	public Airline getAirline() {
		return _a;
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
	 * Updates the number of flights between these two airports. 
	 * @param count  the number of flights
	 * @see ScheduleRoute#getFlights()
	 * @see ScheduleRoute#setRoutes(int)
	 */
	public void setFlights(int count) {
		_frequency = Math.max(0, count);
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

	@Override
	public String getRowClassName() {
		return (_routes == 0) ? "opt1" : null;
	}
	
	/**
	 * Compares two route pairs by comparing their sorted airport codes.
	 * @see ScheduleRoute#toString()
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(ScheduleRoute rp2) {
		return toString().compareTo(rp2.toString());
	}
	
	/**
	 * Compars two route pairs by comparing their airport codes.
	 * @see ScheduleRoute#toString()
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof ScheduleRoute) ? (compareTo((ScheduleRoute) o) == 0) : false;
	}
}
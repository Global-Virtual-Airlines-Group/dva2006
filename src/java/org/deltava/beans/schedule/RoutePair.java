// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

/**
 * A bean to store route pair information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RoutePair implements Comparable<RoutePair> {
	
	private static final String DEP = "$D";
	private static final String ARR = "$A";

	private Airline _a;
	private final Map<String, Airport> _airports = new HashMap<String, Airport>();
	
	/**
	 * Creates a new Route Pair.
	 * @param a the Airline serving this route
	 * @param ad the departure Airport bean
	 * @param aa the arrival Airport bean
	 */
	public RoutePair(Airline a, Airport ad, Airport aa) {
		super();
		_a = a;
		_airports.put(DEP, ad);
		_airports.put(ARR, aa);
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
	 * @see RoutePair#getAirports()
	 */
	public Airport getAirportD() {
		return _airports.get(DEP);
	}

	/**
	 * Returns the arrival Airport.
	 * @return the Airport bean
	 * @see RoutePair#getAirports()
	 */
	public Airport getAirportA() {
		return _airports.get(ARR);
	}
	
	/**
	 * Returns the sorted airports in this route pair. 
	 * @return a Collection of Airport beans
	 * @see RoutePair#getAirportA()
	 * @see RoutePair#getAirportD()
	 */
	public Collection<Airport> getAirports() {
		return _airports.values();
	}

	/**
	 * Compares two route pairs by comparing their sorted airport codes.
	 * @see RoutePair#toString()
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(RoutePair rp2) {
		return toString().compareTo(rp2.toString());
	}
	
	/**
	 * Compars two route pairs by comparing their airport codes.
	 * @see RoutePair#toString()
	 */
	public boolean equals(Object o) {
		return (o instanceof RoutePair) ? (compareTo((RoutePair) o) == 0) : false;
	}
	
	/**
	 * Returns the route pair's hash code.
	 * @see RoutePair#toString()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns the route pair.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<Airport> i = _airports.values().iterator(); i.hasNext(); ) {
			Airport a = i.next();
			buf.append(a.getICAO());
			if (i.hasNext())
				buf.append('-');				
		}
		
		return buf.toString();
	}
}
// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

/**
 * A bean to track which Airports are served by particular Airlines. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportServiceMap {

	private final Map<Airline, Collection<Airport>> _airports = new HashMap<Airline, Collection<Airport>>();

	/**
	 * Adds a particular Airport/Airline pair.
	 * @param a the Airline bean
	 * @param ap the Airport bean
	 */
	public void add(Airline a, Airport ap) {
		Collection<Airport> airports = _airports.get(a);
		if (airports == null) {
			airports = new HashSet<Airport>();
			_airports.put(a, airports);
		}

		airports.add(ap);
	}

	/**
	 * Queries wether a particular Airline serves a particular Airport.
	 * @param ap the Airport bean
	 * @param a the Airline bean
	 * @return TRUE if the Airline serves the Airport, otherwise FALSE
	 */
	public boolean isServiced(Airport ap, Airline a) {
		Collection<Airport> airports = _airports.get(a);
		if (airports == null)
			return false;

		return airports.contains(ap);
	}

	/**
	 * Returns the codes of all Airlines serving a particular Airport.
	 * @param a the Airport bean
	 * @return a Collection of Airline codes
	 * @see Airline#getCode()
	 * @see Airport#setAirlines(Collection)
	 */
	public Collection<String> getAirlineCodes(Airport a) {
		Collection<String> results = new HashSet<String>();
		for (Iterator<Airline> i = _airports.keySet().iterator(); i.hasNext();) {
			Airline al = i.next();
			if (isServiced(a, al))
				results.add(al.getCode());
		}

		return results;
	}
}
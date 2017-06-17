// Copyright 2006, 2007, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

/**
 * A bean to track which Airports are served by particular Airlines. 
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class AirportServiceMap extends TreeMap<Airline, Collection<Airport>> {

	/**
	 * Adds a particular Airport/Airline pair.
	 * @param a the Airline bean
	 * @param ap the Airport bean
	 */
	public void add(Airline a, Airport ap) {
		Collection<Airport> airports = get(a);
		if (airports == null) {
			airports = new HashSet<Airport>();
			put(a, airports);
		}

		airports.add(ap);
	}

	/**
	 * Queries whether a particular Airline serves a particular Airport.
	 * @param ap the Airport bean
	 * @param a the Airline bean
	 * @return TRUE if the Airline serves the Airport, otherwise FALSE
	 */
	public boolean isServiced(Airport ap, Airline a) {
		Collection<Airport> airports = get(a);
		return (airports != null) && airports.contains(ap);
	}

	/**
	 * Returns the codes of all Airlines serving a particular Airport.
	 * @param a the Airport bean
	 * @return a Collection of Airline codes
	 * @see Airline#getCode()
	 * @see Airport#setAirlines(Collection)
	 */
	public Collection<String> getAirlineCodes(Airport a) {
		Collection<String> results = new TreeSet<String>();
		for (Map.Entry<Airline, Collection<Airport>> me : entrySet()) {
			if (me.getValue().contains(a))
				results.add(me.getKey().getCode());
		}

		return results;
	}
}
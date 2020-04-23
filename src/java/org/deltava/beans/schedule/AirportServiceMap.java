// Copyright 2006, 2007, 2012, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A bean to track which Airports are served by particular Airlines. 
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AirportServiceMap extends TreeMap<Airline, Collection<Airport>> {

	/**
	 * Adds a particular Airport/Airline pair.
	 * @param a the Airline bean
	 * @param aps the Airport bean(s)
	 */
	public void add(Airline a, Airport... aps) {
		Collection<Airport> airports = get(a);
		if (airports == null) {
			airports = new HashSet<Airport>();
			put(a, airports);
		}

		for (int x = 0; x < aps.length; x++)
			airports.add(aps[x]);
	}
	
	/**
	 * Queries whether a particular Airline serves a particular Airport.
	 * @param ap the Airport bean
	 * @param a the Airline bean
	 * @return TRUE if the Airline serves the Airport, otherwise FALSE
	 */
	public boolean isServiced(Airport ap, Airline a) {
		return getOrDefault(a, Collections.emptyList()).contains(ap);
	}

	/**
	 * Returns the codes of all Airlines serving a particular Airport.
	 * @param a the Airport bean
	 * @return a Collection of Airline codes
	 * @see Airline#getCode()
	 * @see Airport#setAirlines(Collection)
	 */
	public Collection<String> getAirlineCodes(Airport a) {
		return entrySet().stream().filter(me -> me.getValue().contains(a)).map(me -> me.getKey().getCode()).collect(Collectors.toCollection(TreeSet::new));
	}
}
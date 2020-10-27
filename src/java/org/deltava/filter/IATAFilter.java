// Copyright 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import java.util.*;

import org.deltava.beans.schedule.Airport;

/**
 * An Airport Filter to filter on selected IATA Airport codes. 
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public class IATAFilter implements AirportFilter {
	
	private final Collection<String> _airportCodes = new HashSet<String>();

	/**
	 * Creates the filter.
	 * @param airports a Collection of Airports
	 */
	public IATAFilter(Collection<Airport> airports) {
		super();
		for (Airport a : airports)
			_airportCodes.add(a.getIATA());
	}
	
	/**
	 * Creates the filter.
	 * @param a an Airport to include
	 */
	public IATAFilter(Airport a) {
		super();
		_airportCodes.add(a.getIATA());
	}

	@Override
	public boolean accept(Airport a) {
		return (a != null) && _airportCodes.contains(a.getIATA());
	}
}
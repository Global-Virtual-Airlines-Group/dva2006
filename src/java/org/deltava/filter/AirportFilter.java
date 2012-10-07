// Copyright 2005, 2006, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import java.util.*;

import org.deltava.beans.schedule.Airport;

/**
 * An interface for Airport bean filters.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public abstract class AirportFilter {

	/**
	 * Determines whether the Airport matches the criteria set by this Filter.
	 * @param a the Airport bean to examine
	 * @return TRUE if the airport matches the criteria, otherwise FALSE
	 */
	public abstract boolean accept(Airport a);

	/**
	 * Filters a Collection of Airports.
	 * @param airports the Airports
	 * @return the filtered Collection
	 */
	public Collection<Airport> filter(Collection<Airport> airports) {
		Collection<Airport> results = new LinkedHashSet<Airport>();
		for (Airport ap : airports) {
			if (accept(ap))
				results.add(ap);
		}
		
		return results;
	}
}
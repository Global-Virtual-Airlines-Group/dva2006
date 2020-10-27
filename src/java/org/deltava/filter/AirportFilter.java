// Copyright 2005, 2006, 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.Airport;

/**
 * An interface for Airport bean filters.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public interface AirportFilter {

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
	public default Collection<Airport> filter(Collection<Airport> airports) {
		return airports.stream().filter(this::accept).collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
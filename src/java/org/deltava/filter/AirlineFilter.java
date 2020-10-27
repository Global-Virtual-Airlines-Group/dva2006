// Copyright 2006, 2008, 2009, 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.*;

/**
 * An Airport Filter that filters by Airline.
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public class AirlineFilter implements AirportFilter {
	
	private final Airline _a;

	/**
	 * Creates the Filter.
	 * @param a the Airline to filter on
	 */
	public AirlineFilter(Airline a) {
		super();
		_a = a;
	}

	@Override
	public boolean accept(Airport a) {
		return (a == null) ? false : a.getAirlineCodes().contains(_a.getCode());
	}
}
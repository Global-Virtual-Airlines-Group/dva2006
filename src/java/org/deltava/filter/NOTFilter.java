// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.Airport;

/**
 * An AirportFilter to reverse another AirportFilter.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class NOTFilter extends AirportFilter {
	
	private final AirportFilter _filter;

	/**
	 * Initializes the filter.
	 * @param f the AirportFilter to reverse
	 */
	public NOTFilter(AirportFilter f) {
		_filter = f;
	}

	@Override
	public boolean accept(Airport a) {
		return !_filter.accept(a);
	}
}
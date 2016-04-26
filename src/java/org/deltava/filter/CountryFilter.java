// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.*;

/**
 * An Airport Filter to filter by Country. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class CountryFilter extends AirportFilter {
	
	private final Country _c;

	/**
	 * Creates the Filter.
	 * @param c the Country to filter on
	 */
	public CountryFilter(Country c) {
		super();
		_c = c;
	}
	
	@Override
	public boolean accept(Airport a) {
		return (a == null) ? false : (a.getCountry() == _c);
	}
}
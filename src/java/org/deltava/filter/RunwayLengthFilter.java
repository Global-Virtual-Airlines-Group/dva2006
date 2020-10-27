// Copyright 2013, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.Airport;

/**
 * An airport filter to filter based on maxmimum runway length.
 * @author Luke
 * @version 9.1
 * @since 5.1
 */

public class RunwayLengthFilter implements AirportFilter {
	
	private final int _minRunwayLength;

	/**
	 * Creates the filter.
	 * @param minLength the minimum runway length in feet
	 */
	public RunwayLengthFilter(int minLength) {
		super();
		_minRunwayLength = Math.max(0, minLength);
	}

	@Override
	public boolean accept(Airport a) {
		return (a != null) && ((a.getMaximumRunwayLength() >= _minRunwayLength) || (a.getMaximumRunwayLength() == 0));
	}
}
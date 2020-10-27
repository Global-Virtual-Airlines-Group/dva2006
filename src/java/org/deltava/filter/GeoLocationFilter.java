// Copyright 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

/**
 * An Airport Filter to filter by distance from an arbitrary point. 
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public class GeoLocationFilter implements AirportFilter {
	
	private final GeoPosition _loc;
	private final int _distance;
	
	/**
	 * Creates the Filter.
	 * @param loc the point
	 * @param distance the distance from the point in miles
	 */
	public GeoLocationFilter(GeoLocation loc, int distance) {
		super();
		_loc = new GeoPosition(loc);
		_distance = Math.max(0, distance);
	}
	
	@Override
	public boolean accept(Airport a) {
		return (a == null) ? false : (_loc.distanceTo(a) < _distance);
	}
}
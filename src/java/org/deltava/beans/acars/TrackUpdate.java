// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.GeoLocation;

/**
 * A bean to batch track updates in Redis. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class TrackUpdate {
	
	private final boolean _isACARS;
	private final String _flightID;
	private final GeoLocation _loc;

	/**
	 * Creates the bean.
	 * @param isACARS TRUE if an ACARS flight, otherwise FALSE
	 * @param flightID the flight ID
	 * @param loc the position
	 */
	public TrackUpdate(boolean isACARS, String flightID, GeoLocation loc) {
		super();
		_isACARS = isACARS;
		_flightID = flightID;
		_loc = loc;
	}
	
	/**
	 * Returns if this is an ACARS flight.
	 * @return TRUE if ACARS, otherwise FALSE
	 */
	public boolean isACARS() {
		return _isACARS;
	}
	
	/**
	 * Returns the flight ID.
	 * @return the flight ID
	 */
	public String getFlightID() {
		return _flightID;
	}
	
	/**
	 * Returns the latest position.
	 * @return the position
	 */
	public GeoLocation getLocation() {
		return _loc;
	}
}
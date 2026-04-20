// Copyright 2011, 2012, 2016, 2017, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.flight.Warning;

/**
 * An XACARS Position Report.
 * @author Luke
 * @version 12.2
 * @since 4.1
 */

public class XARouteEntry extends RouteEntry {
	
	private int _flightID;
	private String _msgType;
	
	/**
	 * Creates the route entry.
	 * @param loc the GeoLocation
	 * @param dt the date/time
	 */
	public XARouteEntry(GeoLocation loc, Instant dt) {
		super(loc, dt);
	}
	
	/**
	 * Returns the Flight ID.
	 * @return the flight ID
	 */
	public int getFlightID() {
		return _flightID;
	}
	
	/**
	 * Returns the radar altitude.
	 * @return -1 since unimplemented
	 */
	@Override
	public final int getRadarAltitude() {
		return -1;
	}
	
	public String getMessageType() {
		return _msgType;
	}
	
	/**
	 * Sets the Flight ID.
	 * @param id the ID
	 * @throws IllegalArgumentException if id is zero, negative or already set
	 */
	public void setFlightID(int id) {
		DatabaseBean.validateID(_flightID, id);
		_flightID = id;
	}

	/**
	 * Updats the XACARS message type.
	 * @param mt the type
	 */
	public void setMessageType(String mt) {
		_msgType = mt;
	}
	
	/**
	 * Updates the location.
	 * @param loc the location
	 */
	public void setLocation(GeoLocation loc) {
		_pos = loc;
	}
	
	@Override
	public Collection<Warning> getWarnings() {
		Collection<Warning> warnings = new LinkedHashSet<Warning>();
		if (hasDataError())
			warnings.add(Warning.DATAERROR);
		if ((getAltitude() < 10000) && (getAirSpeed() > 250))
			warnings.add(Warning.OVER250K);
		if (getFuelRemaining() <= 20)
			warnings.add(Warning.NOFUEL);
		if (getAltitude() > 45000)
			warnings.add(Warning.ALTITUDE);
		
		return warnings;
	}
}
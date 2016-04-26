// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * An XACARS Position Report.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class XARouteEntry extends RouteEntry {
	
	private int _flightID;
	private int _vSpeed;
	
	private String _msgType;
	
	/**
	 * Creates the object.
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
	 * Returns the vertical speed.
	 * @return the speed in feet per minute
	 */
	public int getVerticalSpeed() {
		return _vSpeed;
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
	 * Sets the vertical speed.
	 * @param s the speed in feet per minute
	 */
	public void setVerticalSpeed(int s) {
		if ((s >= -7000) && (s <= 7000))
			_vSpeed = s;
	}
	
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
	
	/**
	 * Returns the warning message.
	 * @return the warning
	 */
	@Override
	public String getWarning() {
		Collection<String> warnings = new ArrayList<String>();
		if ((getAltitude() < 10000) && (getAirSpeed() > 250))
			warnings.add("250 UNDER 10K");
		if (getFuelRemaining() <= 20)
			warnings.add("NO FUEL");
		if (getAltitude() > 45000)
			warnings.add("ALTITUDE");
		
		return warnings.isEmpty() ? null : StringUtils.listConcat(warnings, " ");
	}
}
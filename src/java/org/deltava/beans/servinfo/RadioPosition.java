// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store VATSIM transceiver data. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class RadioPosition {
	
	private final String _callsign;
	private final Collection<GeospaceLocation> _positions = new ArrayList<GeospaceLocation>();
	
	/**
	 * Creates the bean.
	 * @param callSign the callsign
	 */
	public RadioPosition(String callSign) {
		super();
		_callsign = callSign;
	}
	
	/**
	 * Returns the user's callsign.
	 * @return the callsign
	 */
	public String getCallsign() {
		return _callsign;
	}

	/**
	 * Returns all of the transceiver locations.
	 * @return a Collection of GeospaceLocation beans
	 */
	public Collection<GeospaceLocation> getPositions() {
		return _positions;
	}

	/**
	 * Returns the average of the transceiver locations.
	 * @return a GeospaceLocation
	 */
	public GeospaceLocation getCenter() {
		if (_positions.isEmpty()) return null;
		double lat = 0; double lng = 0; int alt = 0;
		for (GeospaceLocation loc : _positions) {
			lat += loc.getLatitude();
			lng += loc.getLongitude();
			alt += loc.getAltitude();
		}
		
		return new GeoPosition(lat / _positions.size(), lng / _positions.size(), alt / _positions.size());
	}
	
	/**
	 * Adds a transceiver location.
	 * @param loc the GeospaceLocation
	 */
	public void addPosition(GeospaceLocation loc) {
		_positions.add(loc);
	}
	
	@Override
	public int hashCode() {
		return _callsign.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof RadioPosition) && (hashCode() == o.hashCode()));
	}
}
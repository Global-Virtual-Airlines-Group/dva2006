// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * 
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
	
	public String getCallsign() {
		return _callsign;
	}

	public Collection<GeospaceLocation> getPositions() {
		return _positions;
	}
	
	public GeospaceLocation getCenter() {
		double lat = 0; double lng = 0; int alt = 0;
		for (GeospaceLocation loc : _positions) {
			lat += loc.getLatitude();
			lng += loc.getLongitude();
			alt += loc.getAltitude();
		}
		
		return new GeoPosition(lat / _positions.size(), lng / _positions.size(), alt / _positions.size());
	}
	
	public void addPosition(GeospaceLocation loc) {
		_positions.add(loc);
	}
}
package org.deltava.acars;

import java.util.*;
import java.awt.Point;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.schedule.Airport; 

import org.deltava.util.tile.*;

public class AirportEntryFilter implements RouteEntryFilter {

	private final Collection<Airport> _airports = new HashSet<Airport>();
	private final int _maxDistance;
	private int _maxAlt = Integer.MAX_VALUE;
	
	private final Projection _p;
	
	public AirportEntryFilter(int zoom, int maxDistance, Collection<Airport> airports) {
		super();
		_maxDistance = Math.max(1, maxDistance);
		_p = new MercatorProjection(zoom);
		_airports.addAll(airports);
	}
	
	public void setMaxAltitude(int max) {
		_maxAlt = max;
	}

	@Override
	public Point filter(RouteEntry re) {
		if (re.getRadarAltitude() > _maxAlt) return null;
		
		for (Airport a : _airports) {
			if (a.distanceTo(re) <= _maxDistance)
				return _p.getPixelAddress(re);
		}
		
		return null;
	}
}
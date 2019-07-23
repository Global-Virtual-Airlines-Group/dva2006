// Copyright 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.GeoUtils;

/**
 * A utility class to perform Restricted Airspace validation.
 * @author Luke
 * @version 8.6
 * @since 7.3
 */

@Helper(Airspace.class)
public final class AirspaceHelper {
	
	private static final Logger log = Logger.getLogger(AirspaceHelper.class);
	
	private static final int SEGMENT_SIZE = 75;
	private static final int CLIMB_RATE = 500;

	// static
	private AirspaceHelper() {
		super();
	}
	
	/**
	 * Determines whether a route enters any Prohibited or Restricted airspace.
	 * @param pr a PopulatedRoute
	 * @param includeRestricted TRUE if Restricted as well as Prohibited Airspace should be checked, otherwise FALSE 
	 * @return a Collection of entered Airspace beans
	 */
	public static Collection<Airspace> classify(PopulatedRoute pr, boolean includeRestricted) {
		if (!pr.isPopulated())
			return Collections.emptySet();

		// Caluclate approximate cruise alttiude
		final int cruiseAlt = Math.min(35000, (pr.getDistance() / 2) * CLIMB_RATE);
		
		// Turn into great circle route and approximte climb / descent at 500 ft/mile
		List<GeoLocation> rawGC = new ArrayList<GeoLocation>();
		GeoLocation lastLoc = pr.getAirportD();
		
		// Break the whole route into segments
		for (GeoLocation loc : pr.getWaypoints()) {
			if (loc.distanceTo(lastLoc) > SEGMENT_SIZE)
				rawGC.addAll(GeoUtils.greatCircle(lastLoc, loc, SEGMENT_SIZE));
			else
				rawGC.add(loc);
		
			lastLoc = loc;
		}
			
		// Now do the altitude checks
		Collection<GeospaceLocation> locs = new ArrayList<GeospaceLocation>(rawGC.size());
		for (GeoLocation loc : rawGC) {
			final int apDistance = Math.min(loc.distanceTo(pr.getAirportD()), loc.distanceTo(pr.getAirportA()));
			int maxDistance = (pr.getWaypoints().size() < 4) ? 30 : Math.max(1, Math.min(20, apDistance / 5));
			int dist = lastLoc.distanceTo(loc);
			
			if (dist > maxDistance)
				GeoUtils.greatCircle(lastLoc, loc, maxDistance).stream().map(l -> {
					if (apDistance < SEGMENT_SIZE) {
						int posDst = Math.min(l.distanceTo(pr.getAirportD()), l.distanceTo(pr.getAirportA()));
						int posAlt = Math.max(1000, Math.min(cruiseAlt, posDst * CLIMB_RATE));
						return new GeoPosition(l.getLatitude(), l.getLongitude(), posAlt);
					}
					
					return new GeoPosition(l.getLatitude(), l.getLongitude(), cruiseAlt);
				}).forEachOrdered(locs::add);
			else
				locs.add(new GeoPosition(loc.getLatitude(), loc.getLongitude(), Math.max(1000, Math.min(cruiseAlt, apDistance * CLIMB_RATE))));
			
			lastLoc= loc;
		}

		log.info(pr.getWaypoints().size() + " => " + locs.size());
		return classify(locs, includeRestricted);
	}
	
	/**
	 * Determines whether a particular set of GeoLocations is within any Prohibited or Restricted airspace. 
	 * @param locs a Collection of GeospaceLocations
	 * @param includeRestricted TRUE if Restricted as well as Prohibited Airspace should be checked, otherwise FALSE
	 * @return a Collection of entered Airspace beans 
	 */
	public static Collection<Airspace> classify(Collection<? extends GeospaceLocation> locs, final boolean includeRestricted) {
		return locs.parallelStream().map(loc -> Airspace.isRestricted(loc)).filter(Objects::nonNull).filter(a -> { return includeRestricted || (a.getType() == AirspaceType.P); }).collect(Collectors.toSet());
	}
}
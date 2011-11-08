// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.util.GeoUtils;

/**
 * A utility class to do ETOPS validation. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

@Helper(RoutePair.class)
public final class ETOPSHelper {
	
	private static final List<Airport> _airports = new ArrayList<Airport>();
	
	private static final Aircraft DUMMY = new Aircraft("ETOPS") {{ setEngines((byte) 2); }};

	// singleton
	private ETOPSHelper() {
		super();
	}
	
	/**
	 * Initializes the set of diversion airports.
	 * @param airports a Collection of Airports
	 */
	public static synchronized void init(Collection<Airport> airports) {
		_airports.clear();
		Collection<Airport> apSet = new HashSet<Airport>(airports);
		_airports.addAll(apSet);
	}

	/**
	 * Validates whether a Route Pair should trigger an ETOPS warning.
	 * @param a the Aircraft used, or null for a generic 2-engine aircraft
	 * @param rp the RoutePair
	 * @return TRUE if an ETOPS warning should be triggered, otherwise FALSE
	 * @throws NullPointerException if rp is null 
	 */
	public static boolean validate(Aircraft a, RoutePair rp) {
		if (a == null) a = DUMMY;
		
		// Check if aircraft is ETOPS or >2 engines
		if (a.getETOPS() || (a.getEngines() > 2))
			return false;
		
		// Check for North/South Atlantic - exclude Iceland/Greenland runs
		if (rp.crosses(-30)) {
			double lat = GeoUtils.meridianLatitude(rp.getAirportD(), rp.getAirportA(), -30);
			return ((lat < 61) || rp.crosses(-55));
		}
		
		// Check for Indian Ocean
		if (rp.crosses(75)) {
			double lat = GeoUtils.meridianLatitude(rp.getAirportD(), rp.getAirportA(), 75);
			return (lat < -1);
		}
		
		// Check for Eastern Pacific
		if (rp.crosses(-143)) {
			double lat = GeoUtils.meridianLatitude(rp.getAirportD(), rp.getAirportA(), -143);
			return (lat > 55);
		}
		
		// Check for Western Pacific
		if (rp.crosses(172)) {
			double lat = GeoUtils.meridianLatitude(rp.getAirportD(), rp.getAirportA(), 172);
			return (lat > -1);
		}
		
		return false;
	}

	/**
	 * ETOPS-classifies a set of positions. 
	 * @param entries a Collection of GeoLocations
	 * @return an ETOPS classification
	 */
	public static ETOPS classify(Collection<? extends GeoLocation> entries) {
		if (entries.size() < 2)
			return ETOPS.ETOPS60;

		// Get the starting point
		Iterator<? extends GeoLocation> i = entries.iterator();
		GeoComparator cmp = new GeoComparator(i.next());
		
		// Copy airports into a sortable collection
		Airport[] airports = _airports.toArray(new Airport[0]);
		Arrays.sort(airports, cmp);
		
		// Iterate through the list and determine the closest airport
		ETOPS result = ETOPS.ETOPS60;
		while (i.hasNext()) {
			GeoLocation pos = i.next();
			int gap = GeoUtils.distance(cmp.getLocation(), pos);
			if (gap > 40) {
				cmp = new GeoComparator(pos);
				Arrays.sort(airports, cmp);
			}
			
			// Get the distance/ETOPS classification to the closest airport
			int dist = GeoUtils.distance(pos, airports[0]);
			ETOPS e = ETOPS.getClassification(dist);
			if (e == null)
				return null;
			else if (e.ordinal() > result.ordinal())
				result = e;
		}
		
		return result;
	}

	/**
	 * ETOPS-classifies an ACARS route. 
	 * @param pr a PopulatedRoute bean
	 * @return an ETOPS classification
	 */
	public static ETOPS classify(PopulatedRoute pr) {
		Collection<GeoLocation> entries = new LinkedHashSet<GeoLocation>();
		Collection<? extends GeoLocation> wps = pr.getWaypoints();
		Iterator<? extends GeoLocation> i = wps.iterator(); GeoLocation lastPos = i.next(); entries.add(lastPos);
		while (i.hasNext()) {
			GeoLocation pos = i.next();
			int dist = GeoUtils.distance(lastPos, pos);
			if (dist > 30)
				entries.addAll(GeoUtils.greatCircle(lastPos, pos, 30));
			else
				entries.add(pos);
			
			lastPos = pos;
		}
		
		return classify(entries);
	}
}
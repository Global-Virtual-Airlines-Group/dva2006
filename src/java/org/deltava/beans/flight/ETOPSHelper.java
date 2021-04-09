// Copyright 2011, 2012, 2014, 2015, 2016, 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.util.GeoUtils;

/**
 * A utility class to do ETOPS validation.
 * @author Luke
 * @version 10.0
 * @since 4.1
 */

@Helper(RoutePair.class)
public final class ETOPSHelper {

	private static final List<Airport> _airports = new ArrayList<Airport>();
	
	private static class WarningPoint extends NavigationDataBean {
		
		private final List<Airport> _closestAirports = new ArrayList<Airport>();
		
		WarningPoint(GeoLocation loc, Airport... aps) {
			super(Navaid.INT, loc.getLatitude(), loc.getLongitude());
			setCode("ETOPS WARNING POINT");
			_closestAirports.addAll(Arrays.asList(aps));
		}

		@Override
		public String getIconColor() {
			return RED;
		}

		@Override
		public String getInfoBox() {
			StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
			buf.append(getHTMLTitle());
			buf.append(getHTMLPosition());
			if (!_closestAirports.isEmpty()) buf.append("<br />");
			for (Iterator<Airport> i = _closestAirports.iterator(); i.hasNext(); ) {
				Airport a = i.next();
				buf.append("Distance from ");
				buf.append(a.toString());
				buf.append(": ");
				buf.append(distanceTo(a));
				buf.append(" miles");
				if (i.hasNext())
					buf.append("<br />");
			}
			
			buf.append("</div>");
			return buf.toString();
		}

		@Override
		public int getPaletteCode() {
			return 3;
		}

		@Override
		public int getIconCode() {
			return 33;
		}
	}
	
	private static class ClosestAirport extends AirportLocation {
		
		ClosestAirport(Airport a) {
			super(a);
		}
		
		@Override
		public int getPaletteCode() {
			return 2;
		}

		@Override
		public int getIconCode() {
			return 56;
		}
	}
	
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
		_airports.addAll(new HashSet<Airport>(airports));
	}

	/**
	 * Validates whether an ETOPS classification should trigger an ETOPS warning.
	 * @param ae the Aircraft ETOPS rating
	 * @param re the route ETOPS classification
	 * @return TRUE if an ETOPS warning should be triggered, otherwise FALSE
	 * @throws NullPointerException if rp is null
	 */
	public static boolean isWarn(ETOPS ae, ETOPS re) {
		return (ae != null) && (re != null) && (ae != ETOPS.INVALID) && (re.getTime() > ae.getTime());
	}

	/**
	 * ETOPS-classifies a set of positions.
	 * @param entries a Collection of GeoLocations
	 * @return an ETOPS classification
	 */
	public static ETOPSResult classify(Collection<? extends GeoLocation> entries) {
		if (entries.size() < 2)
			return new ETOPSResult(ETOPS.ETOPS60);

		// Get the starting point
		Iterator<? extends GeoLocation> i = entries.iterator();
		GeoComparator cmp = new GeoComparator(i.next());

		// Copy airports into a sortable collection
		Airport[] airports = _airports.toArray(new Airport[0]);
		Arrays.sort(airports, cmp);

		// Iterate through the list and determine the closest airport
		ETOPS result = ETOPS.ETOPS60;
		Airport[] closestAirports = new Airport[2];
		GeoLocation maxPT = null;
		int maxDistance = 0;
		Collection<String> msgs = new LinkedHashSet<String>();
		while (i.hasNext()) {
			GeoLocation pos = i.next();
			int gap = pos.distanceTo(cmp.getLocation());
			if (gap > 30) {
				cmp = new GeoComparator(pos);
				Arrays.sort(airports, cmp);
			}

			// Get the distance to the closest airport
			int dist = pos.distanceTo(airports[0]);
			if (dist > maxDistance) {
				maxDistance = dist;
				closestAirports[0] = airports[0];
				closestAirports[1] = airports[1];
				maxPT = pos;
			}

			// Get ETOPS classification
			ETOPS e = ETOPS.getClassification(dist);
			if (e.ordinal() > result.ordinal()) {
				msgs.add(e.toString() + " - " + airports[0].toString() + " - " + dist + " > " + result.getRange());
				result = e;
			}
		}
		
		WarningPoint wp = (maxPT == null) ? null : new WarningPoint(maxPT, closestAirports[0], closestAirports[1]);
		ETOPSResult r = new ETOPSResult(result, wp, msgs);
		if (wp != null) {
			r.add(new ClosestAirport(closestAirports[0]));
			r.add(new ClosestAirport(closestAirports[1]));
		}
		
		return r;
	}

	/**
	 * ETOPS-classifies an ACARS route.
	 * @param pr a PopulatedRoute bean
	 * @return an ETOPS classification
	 */
	public static ETOPSResult classify(PopulatedRoute pr) {
		Collection<GeoLocation> entries = new LinkedHashSet<GeoLocation>();
		entries.add(pr.getAirportD());
		Collection<? extends GeoLocation> wps = pr.getWaypoints();
		if (!wps.isEmpty()) {
			Iterator<? extends GeoLocation> i = wps.iterator();
			GeoLocation lastPos = i.next();
			entries.add(lastPos);
			while (i.hasNext()) {
				GeoLocation pos = i.next();
				int dist = lastPos.distanceTo(pos);
				if (dist > 30)
					entries.addAll(GeoUtils.greatCircle(lastPos, pos, 30));
				else
					entries.add(pos);

				lastPos = pos;
			}
		} else if (pr.getAirportA() != null)
			entries.addAll(GeoUtils.greatCircle(pr.getAirportD(), pr.getAirportA(), 30));

		if (pr.getAirportA() != null)
			entries.add(pr.getAirportA());
		
		return classify(entries);
	}
}
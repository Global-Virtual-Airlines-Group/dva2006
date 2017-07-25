// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Helper;

import org.deltava.util.*;

/**
 * A utility class that uses Dijkstra's algorithm to calculate the shortest multi-airport leg.
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

@Helper(RoutePair.class)
public class RoutePathHelper {
	
	private static final Logger log = Logger.getLogger(RoutePathHelper.class);
	
	private final Map<Airport, Collection<Airport>> _links = new HashMap<Airport, Collection<Airport>>();
	
	private class Vertex implements Comparable<Vertex> {
		private final Airport _a;
		private Vertex _prev;
		private int _distance = Integer.MAX_VALUE;
		
		Vertex(Airport a) {
			_a = a;
		}
		
		public Airport getAirport() {
			return _a;
		}
		
		public int getDistance() {
			return _distance;
		}
		
		public Vertex getPrevious() {
			return _prev;
		}
		
		public void setDistance(int d) {
			_distance = Math.max(0, d);
		}
		
		public void setPrevious(Vertex v) {
			_prev = v;
		}
		
		@Override
		public String toString() {
			return _a.getICAO();
		}

		@Override
		public int compareTo(Vertex v) {
			if (_a.equals(v._a)) return 0;
			int tmpResult = Integer.valueOf(_distance).compareTo(Integer.valueOf(v._distance));
			return (tmpResult == 0) ? _a.compareTo(v._a) : tmpResult;
		}
	}
	
	public RoutePathHelper() {
		super();
	}
	
	public void setLinks(Airport a, Collection<Airport> links) {
		_links.put(a, links);
	}

	/**
	 * Calculates the shortest path between two Airports.
	 * @param rp the RoutePair
	 * @return a Collection of RoutePair legs, or empty if no route found
	 */
	public Collection<RoutePair> getShortestPath(RoutePair rp) {
		
		Vertex v = new Vertex(rp.getAirportD());
		v.setDistance(0);

		Collection<Vertex> uvV = new TreeSet<Vertex>() {{ add(v); }};
		Map<Airport, Vertex> uvA = new HashMap<Airport, Vertex>() {{ put(rp.getAirportD(), v); }};
		_links.keySet().forEach(ap -> { Vertex vx = new Vertex(ap); uvV.add(vx); uvA.put(ap, vx); });
				
		// Loop through the unvisited
		uvV.stream().forEach(vv -> {
			log.info("Starting at " + vv);
			Collection<Airport> neighbors = _links.get(vv.getAirport());
			for (Airport aN : neighbors) {
				Vertex vN = uvA.get(aN);
				if (vN == null)  continue;
				
				int distance = (vv.getDistance() == Integer.MAX_VALUE ? 0 : vv.getDistance()) + GeoUtils.distance(vv.getAirport(), aN);
				if (distance < vN.getDistance()) {
					if (vN.getDistance() == Integer.MAX_VALUE)
						log.info("Shortest path to " + vN + " is from " + vv + ", distance=" + distance);
					else
						log.info("Decreasing shortest path from " + rp.getAirportD().getICAO() + " to " + vN + " from " + vv + "(was " + vN + ") distance=" + vN.getDistance() + " to " + distance);
						
					vN.setDistance(distance);
					vN.setPrevious(vv);
				}
			}
		});
		
		// Work backwards from the destination
		Vertex vA = uvA.get(rp.getAirportA());
		if (vA.getPrevious() == null)
			return Collections.emptySet();
		
		Collection<RoutePair> route = new ArrayList<RoutePair>();
		while (vA.getPrevious() != null) {
			RoutePair rt = new ScheduleRoute(vA.getAirport(), vA.getPrevious().getAirport());
			route.add(rt);
			log.info(rt);
			if ((vA.getPrevious() != null) && (vA.getPrevious().getPrevious() == vA)) {
				log.warn("Loop between " + vA + " and " + vA.getPrevious());
				break;
			}
				
			vA = vA.getPrevious();
		}
		
		log.info("Route is " + route);
		return route;
	}
}
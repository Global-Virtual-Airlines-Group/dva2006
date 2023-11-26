// Copyright 2017, 2018, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.apache.logging.log4j.*;
import org.deltava.beans.Helper;

import org.deltava.util.CollectionUtils;

/**
 * A utility class that uses Dijkstra's algorithm to calculate the shortest multi-airport leg.
 * @author Luke
 * @version 11.1
 * @since 7.5
 */

@Helper(RoutePair.class)
public final class RoutePathHelper {
	
	private static final Logger log = LogManager.getLogger(RoutePathHelper.class);
	
	private final Map<Airport, Collection<ScheduleRoute>> _links = new HashMap<Airport, Collection<ScheduleRoute>>();
	
	private final int _legCost;
	private final int _historicCost;
	
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
		public int hashCode() {
			return _a.getICAO().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			return ((o instanceof Vertex v) && _a.equals(v._a));
		}

		@Override
		public int compareTo(Vertex v) {
			if (_a.equals(v._a)) return 0;
			int tmpResult = Integer.compare(_distance, v._distance);
			return (tmpResult == 0) ? _a.compareTo(v._a) : tmpResult;
		}
	}
	
	/**
	 * Creates the helper bean.
	 * @param legCost the flat extra cost of each additional leg in miles, to prioritize fewer legs over absolute distance
	 * @param historicCost the flat extra cost of route pair served only by historical flights
	 */
	public RoutePathHelper(int legCost, int historicCost) {
		super();
		_legCost = legCost;
		_historicCost = historicCost;
	}
	
	/**
	 * Populates the links between the airports.
	 * @param links a Collection of ScheduleRoutes
	 */
	public void setLinks(Collection<ScheduleRoute> links) {
		links.forEach(sr -> CollectionUtils.addMapCollection(_links, sr.getAirportD(), sr));
	}
	
	/**
	 * Calculates the shortest path between two Airports.
	 * @param rp the RoutePair
	 * @return a Collection of RoutePair legs, or empty if no route found
	 */
	public Collection<RoutePair> getShortestPath(RoutePair rp) {
		
		Vertex v = new Vertex(rp.getAirportD());
		v.setDistance(0);

		List<Vertex> Q = new ArrayList<Vertex>(_links.size() + 2) {{ add(v); }};
		Map<Airport, Vertex> uvA = new HashMap<Airport, Vertex>() {{ put(rp.getAirportD(), v); }};
		_links.keySet().forEach(ap -> { 
			Vertex vx = new Vertex(ap); uvA.put(ap, vx);
			if (!ap.equals(rp.getAirportD())) Q.add(vx);
		});
				
		// Loop through the unvisited
		while (!Q.isEmpty()) {
			Collections.sort(Q);			
			Vertex u = Q.get(0);
			Q.remove(0);
			if (u.getAirport().equals(rp.getAirportA())) {
				log.info("Skipping {}", u);
				continue;
			} else if (u.getDistance() == Integer.MAX_VALUE) {
				log.info("No routes to {}", u);
				continue;
			}
			
			Collection<ScheduleRoute> links = _links.get(u.getAirport());
			for (ScheduleRoute srt : links) {
				Vertex vN = uvA.get(srt.getAirportA());
				if (vN == null) continue;
				
				boolean isHistoricLeg = (srt.getFlights() == srt.getRoutes());
				int distance = u.getDistance() + srt.getDistance() + (isHistoricLeg ? _historicCost : _legCost);
				if (distance < vN.getDistance() && (distance > 0)) {
					if (vN.getPrevious() != null)
						log.info("Distance to {} is now {} from {}, was {} from {}", vN, Integer.valueOf(distance), u, Integer.valueOf(vN.getDistance()), vN.getPrevious());
					
					vN.setDistance(distance);
					vN.setPrevious(u);
				}
			}
		}
		
		// Work backwards from the destination
		Vertex vA = uvA.get(rp.getAirportA());
		if ((vA == null) || (vA.getPrevious() == null))
			return Collections.emptyList();
		
		List<RoutePair> route = new ArrayList<RoutePair>();
		while (vA.getPrevious() != null) {
			RoutePair rt = new ScheduleRoute(vA.getPrevious().getAirport(), vA.getAirport());
			route.add(rt);
			if ((vA.getPrevious() != null) && (vA.getPrevious().getPrevious() == vA))
				throw new IllegalStateException("Loop between " + vA + " and " + vA.getPrevious());
				
			vA = vA.getPrevious();
		}
		
		Collections.reverse(route);
		log.info(route);
		return route;
	}
}
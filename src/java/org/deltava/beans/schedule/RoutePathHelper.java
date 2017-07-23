// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.Helper;

import org.deltava.util.GeoUtils;

/**
 * A utility class that uses Dijkstra's algorithm to calculate the shortest multi-airport leg.
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

@Helper(RoutePair.class)
public class RoutePathHelper {
	
	private final Map<Airport, Collection<Airport>> _links = new HashMap<Airport, Collection<Airport>>();
	
	private class Vertex {
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
			return _a.toString();
		}
	}
	
	private class VertexComparator implements Comparator<Vertex> {

		VertexComparator() {
			super();
		}
		
		@Override
		public int compare(Vertex v1, Vertex v2) {
			if (v1.getAirport().equals(v2.getAirport())) return 0;
			int tmpResult = (v1.getDistance() == v2.getDistance()) ? 0 : ((v1.getDistance() > v2.getDistance()) ? 1 : -1);
			return (tmpResult == 0) ? v1.getAirport().compareTo(v2.getAirport()) : tmpResult;
		}
	}

	// static class
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

		Collection<Vertex> uvV = new TreeSet<Vertex>(new VertexComparator());
		Map<Airport, Vertex> uvA = new HashMap<Airport, Vertex>();
		Collection<Vertex> all = new ArrayList<Vertex>(_links.size());
		_links.keySet().forEach(ap -> { Vertex vx = new Vertex(ap); uvV.add(vx); all.add(vx); uvA.put(ap, vx); });
		uvA.put(rp.getAirportD(), v);
		uvV.add(v);
				
		// Loop through the unvisited
		uvV.stream().forEach(vv -> {
			Collection<Airport> neighbors = _links.get(vv.getAirport());
			for (Airport aN : neighbors) {
				Vertex vN = uvA.get(aN);
				if ((vN == null) || (vN.getDistance() < Integer.MAX_VALUE)) continue;
				int distance = vv.getDistance() + GeoUtils.distance(vv.getAirport(), aN);
				if (distance < vN.getDistance()) {
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
			route.add(new ScheduleRoute(vA.getAirport(), vA.getPrevious().getAirport()));
			vA = vA.getPrevious();
		}
		
		return route;
	}
}
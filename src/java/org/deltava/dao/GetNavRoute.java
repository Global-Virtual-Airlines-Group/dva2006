// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load routes. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class GetNavRoute extends GetNavAirway {
	
	private static final Logger log = Logger.getLogger(GetNavRoute.class);
	
	private static final Cache<Route> _rCache = new AgingCache<Route>(320);

	private class CacheableRoute implements Route {

		private String _route;
		private final LinkedList<NavigationDataBean> _waypoints = new LinkedList<NavigationDataBean>();

		CacheableRoute(String route, Collection<NavigationDataBean> waypoints) {
			super();
			_route = route.toUpperCase();
			_waypoints.addAll(waypoints);
		}
		
		public void addWaypoint(NavigationDataBean nd) {
			_waypoints.add(nd);
		}

		public LinkedList<NavigationDataBean> getWaypoints() {
			return new LinkedList<NavigationDataBean>(_waypoints);
		}
		
		public String getRoute() {
			return _route;
		}
		
		public int getSize() {
			return _waypoints.size();
		}

		public Object cacheKey() {
			return new Integer(_route.hashCode());
		}
		
		public String toString() {
			return _route;
		}
		
		public boolean equals(Object o) {
			return (o != null) && (_route.equals(o.toString()));
		}
	}
	
	private class ExternalPopulatedRoute extends PopulatedRoute implements ExternalFlightRoute {
		
		private String _source;
		
		ExternalPopulatedRoute() {
			super();
		}

		public String getSource() {
			return _source;
		}
		
		public void setSource(String src) {
			_source = src;
		}
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavRoute(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _rCache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _rCache.getHits();
	}

	/**
	 * Returns all waypoints for a route, expanding Airways but <i>NOT</i> SID/STARs.
	 * @param route the space-delimited route
	 * @param start the starting point
	 * @return an ordered List of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<NavigationDataBean> getRouteWaypoints(String route, GeoLocation start) throws DAOException {
		if (route == null)
			return new LinkedList<NavigationDataBean>();

		// Check the cache
		Route obj = _rCache.get(route);
		if (obj instanceof CacheableRoute) {
			CacheableRoute cr = (CacheableRoute) obj;
			return cr.getWaypoints();
		} else if (obj != null)
			obj = null;

		// Get the route text
		List<String> tkns = StringUtils.split(route, " ");
		GeoLocation lastPosition = start;
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		for (int x = 0; x < tkns.size(); x++) {
			String wp = tkns.get(x);
			
			// Check if we're referencing an airway
			Collection<Airway> aws = getAirways(wp);
			if ((wp.indexOf('.') != -1) && ((x == 0) || (x == (tkns.size() - 1)))) {
				TerminalRoute tr = getRoute(wp);
				if (tr != null) {
					if ((tr.getType() == TerminalRoute.SID) && (x < (tkns.size() - 1)))
						routePoints.addAll(tr.getWaypoints(tkns.get(x + 1)));
					else if ((tr.getType() == TerminalRoute.STAR) && (x > 0))
						routePoints.addAll(tr.getWaypoints(tkns.get(x - 1)));
					else
						routePoints.addAll(tr.getWaypoints());
				}
			} else if (aws.size() > 0) {
				Airway aw = null;
				
				// Find the closest one if there's more than one
				if (aws.size() > 1) {
					GeoComparator gp = new GeoComparator(lastPosition);
					SortedSet<Airway> airways = new TreeSet<Airway>(gp);
					airways.addAll(aws);
					aw = airways.first();
				} else
					aw = aws.iterator().next();
				
				// Get the waypoints
				String endPoint = (x < (tkns.size() - 1)) ? tkns.get(x + 1) : "";
				Collection<NavigationDataBean> awPoints = aw.getWaypoints((x == 0) ? wp : tkns.get(x - 1), endPoint);
				routePoints.addAll(awPoints);
			} else {
				NavigationDataMap navaids = get(wp);
				NavigationDataBean nd = navaids.get(wp, lastPosition);
				if (nd != null) {
					routePoints.add(nd);
					lastPosition = nd;
				}
			}
		}

		// Add to the cache and return the waypoints
		if (routePoints.size() > 1)
			_rCache.add(new CacheableRoute(route, routePoints));
		
		return new LinkedList<NavigationDataBean>(routePoints);
	}
	
	/**
	 * Populates a flight route with waypoint beans.
	 * @param rt the FlightRoute bean to populate
	 * @return a populated DispatchRoute
	 * @throws DAOException if a JDBC error occurs
	 */
	public PopulatedRoute populate(FlightRoute rt) throws DAOException {

		PopulatedRoute pr = null; 
		if (rt instanceof ExternalFlightRoute) {
			pr = new ExternalPopulatedRoute();
			ExternalFlightRoute efr = (ExternalFlightRoute) pr;
			efr.setSource(((ExternalFlightRoute) rt).getSource());
		} else
			pr = new PopulatedRoute();
		
		// Copy fields
		pr.setAirportD(rt.getAirportD());
		pr.setAirportA(rt.getAirportA());
		pr.setComments(rt.getComments());
		pr.setCreatedOn(rt.getCreatedOn());
		pr.setCruiseAltitude(rt.getCruiseAltitude());
		pr.setRoute(rt.getRoute());

		// Load the SID
		if (!StringUtils.isEmpty(rt.getSID()) && (rt.getSID().contains("."))) {
			List<String> tkns = StringUtils.split(rt.getSID(), ".");
			String rwy = (tkns.size() > 2) ? tkns.get(2) : null;
			log.info("Searching for best SID for " + rt.getSID() + " runway " + rwy);
			TerminalRoute sid = getBestRoute(pr.getAirportD(), TerminalRoute.SID, tkns.get(0), tkns.get(1), rwy);
			if (sid != null) {
				log.info("Found " + sid.getCode());
				pr.setSID(sid.getCode());
			}
		}
		
		// Load the route waypoints
		List<NavigationDataBean> points = getRouteWaypoints(pr.getRoute(), pr.getAirportD());
		for (NavigationDataBean nd : points)
			pr.addWaypoint(nd, nd.getAirway());
		
		// Load best STAR
		if (!StringUtils.isEmpty(rt.getSTAR()) && (rt.getSTAR().contains("."))) {
			StringTokenizer tkns = new StringTokenizer(rt.getSTAR(), ".");
			TerminalRoute star = getBestRoute(pr.getAirportA(), TerminalRoute.STAR, tkns.nextToken(), tkns.nextToken(), (String) null);
			if (star != null) {
				pr.setSTAR(star.getCode());
				for (NavigationDataBean nd : star.getWaypoints())
					pr.addWaypoint(nd, star.getCode());
			}
		}
		
		return pr;
	}
}
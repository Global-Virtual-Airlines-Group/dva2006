// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load routes. 
 * @author Luke
 * @version 3.4
 * @since 2.6
 */

public class GetNavRoute extends GetOceanicRoute {
	
	private static final Cache<Route> _rCache = new AgingCache<Route>(1024);
	
	private java.util.Date _effectiveDate;

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
		
		public int hashCode() {
			return _route.hashCode();
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
	 * Sets the effective date when looking up Oceanic Tracks.
	 * @param dt the effective date/time
	 */
	public void setEffectiveDate(java.util.Date dt) {
		_effectiveDate = dt;
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_rCache);
	}
	
	/**
	 * Clears the cache.
	 */
	public void clear() {
		_rCache.clear();
		super.clear();
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
		Route obj = _rCache.get(new Integer(route.hashCode()));
		if (obj instanceof CacheableRoute) {
			CacheableRoute cr = (CacheableRoute) obj;
			return cr.getWaypoints();
		} else if (obj != null)
			obj = null;

		// Get the route text
		List<String> tkns = StringUtils.split(route, " ");
		GeoLocation lastPosition = start; Collection<Airway> aws = new ArrayList<Airway>();
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		for (int x = 0; x < tkns.size(); x++) {
			String wp = tkns.get(x);
			aws.clear();
			
			// Check if we're referencing a NAT route. Even if we can't find a perfect route, find one that
			// starts at the starting point, and replace the next point on the NAT with the endpoint of this NAT
			if ((x > 0) && ((wp.startsWith("NAT") || wp.startsWith("PACOT")))) {
				OceanicTrackInfo.Type rType = wp.startsWith("NAT") ? OceanicTrackInfo.Type.NAT : OceanicTrackInfo.Type.PACOT; 
				DailyOceanicTracks tracks = getOceanicTracks(rType, _effectiveDate);
				if (tracks.size() == 0)
					tracks = getOceanicTracks(rType, null);
				
				// Find either the specified NAT track, or the one that matches the waypoints
				String prevWP = tkns.get(x - 1);
				String nextWP = (x < (tkns.size() - 1)) ? tkns.get(x + 1) : null;
				OceanicTrack tr = tracks.getTrack(wp);
				if ((tr == null) || (!tr.contains(prevWP) && !tr.contains(nextWP))) {
					tr = tracks.find(prevWP, nextWP);
					if (tr == null) {
						tr = tracks.find(prevWP, null);
						if ((tr != null) && tracks.contains(nextWP))
							tkns.set(x+1, tr.getEnd().getCode());
						else if ((tr != null) && (nextWP != null)) {
							NavigationDataMap ndmap = get(nextWP);
							NavigationDataBean nd = ndmap.get(wp, tr.getEnd());
							if (nd != null)
								tkns.set(x+1, nd.getCode());
						}
					}
				}

				if (tr != null)
					aws.add(tr);
			} else
				aws.addAll(getAirways(wp));
			
			// Check if we're referencing an airway
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
		} else if (rt instanceof DispatchRoute) {
			DispatchRoute dr = (DispatchRoute) rt;
			DispatchRoute pdr = new DispatchRoute();
			pdr.setUseCount(dr.getUseCount());
			pdr.setDispatchBuild(dr.getDispatchBuild());
			pr = pdr;
		} else
			pr = new PopulatedRoute();
		
		// Copy fields
		pr.setAirportD(rt.getAirportD());
		pr.setAirportA(rt.getAirportA());
		pr.setComments(rt.getComments());
		pr.setCreatedOn(rt.getCreatedOn());
		pr.setCruiseAltitude(rt.getCruiseAltitude());
		pr.setRoute(rt.getRoute());
		if (rt.getID() > 0)
			pr.setID(rt.getID());
		
		// Load SID
		List<String> wpCodes = StringUtils.split(pr.getRoute(), " ");
		if (!StringUtils.isEmpty(rt.getSID()) && (rt.getSID().contains("."))) {
			TerminalRoute sid = getRoute(rt.getSID()); 
			if (sid != null) {
				pr.setSID(rt.getSID());
				String transition = wpCodes.isEmpty() ? sid.getTransition() : wpCodes.get(0);
				for (NavigationDataBean nd : sid.getWaypoints(transition)) {
					pr.addWaypoint(nd, sid.getCode());
					wpCodes.remove(nd.getCode());
				}
			}
		}
		
		// Load the route waypoints
		List<NavigationDataBean> points = getRouteWaypoints(StringUtils.listConcat(wpCodes, " "), pr.getAirportD());
		for (NavigationDataBean nd : points)
			pr.addWaypoint(nd, nd.getAirway());
		
		// Load STAR
		if (!StringUtils.isEmpty(rt.getSTAR()) && (rt.getSTAR().contains("."))) {
			TerminalRoute star = getRoute(rt.getSTAR());
			if (star != null) {
				pr.setSTAR(rt.getSTAR());
				String transition = wpCodes.isEmpty() ? star.getTransition() : wpCodes.get(wpCodes.size() - 1);
				for (NavigationDataBean nd : star.getWaypoints(transition))
					pr.addWaypoint(nd, star.getCode());
			}
		}
		
		return pr;
	}
}
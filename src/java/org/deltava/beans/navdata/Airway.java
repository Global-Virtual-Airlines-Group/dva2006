// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Airway names and waypoints.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Airway implements Comparable, Cacheable, Route {

	private String _code;
	private final List<String> _waypoints = new ArrayList<String>();

	/**
	 * Creates a new Airway bean.
	 * @param code the airway code
	 * @throws NullPointerException if code is null
	 */
	public Airway(String code) {
		super();
		setCode(code);
	}

	/**
	 * Creates a new Airway bean with a route.
	 * @param code the airway code
	 * @param route a space-delimited list of waypoints
	 * @throws NullPointerException if code or route are null
	 * @see Airway#setRoute(String)
	 */
	public Airway(String code, String route) {
		this(code);
		setRoute(route);
	}

	/**
	 * Returns the airway code.
	 * @return the code
	 */
	public String getCode() {
		return _code;
	}

	/**
	 * Returns the number of waypoints in the airway.
	 * @return the number of waypoints
	 */
	public int getSize() {
		return _waypoints.size();
	}

	/**
	 * Returns the waypoint <i>codes</i> for this Airway. <i>The returned collection is mutable.</i>
	 * @return an ordered Collection of waypoint codes
	 * @see Airway#getRoute()
	 * @see Airway#addWaypoint(String)
	 * @see Airway#setRoute(String)
	 */
	public Collection<String> getWaypoints() {
		return new LinkedHashSet<String>(_waypoints);
	}
	
	/**
	 * Returns the waypoint <i>beans</i> for this Airway. <i>The returned collection is mutable.</i>
	 * @param ndmap the NavigationDataMap containing the beans
	 * @param lastPosition the end of the Airway for dupe filtering
	 * @return an ordered Collection of beans
	 */
	public Collection<NavigationDataBean> getWaypoints(NavigationDataMap ndmap, GeoLocation lastPosition) {
	   // Iterate through the waypoint codes
	   Collection<NavigationDataBean> results = new LinkedHashSet<NavigationDataBean>();
		for (Iterator<String> i = _waypoints.iterator(); i.hasNext();) {
			String trwp = i.next();
			NavigationDataBean nd = ndmap.get(trwp, lastPosition);
			if (nd != null) {
				results.add(nd);
				lastPosition = nd;
			}
		}
		
		return results;
	}
	
	/**
	 * Returns the waypoint <i>beans</i> for this Airway. <i>The returned collection is mutable.</i>
	 * @param ndmap the NavigationDataMap containing the beans
	 * @return an ordered Collection of beans
	 */
	public Collection<NavigationDataBean> getWaypoints(NavigationDataMap ndmap) {
		return getWaypoints(ndmap, null);
	}

	/**
	 * Returns a subset of waypoint <i>codes</i> for this Airway between two waypoints.
	 * @param start the starting waypoint code
	 * @param end the ending waypoint code
	 * @return a List of waypoint codes
	 * @throws NullPointerException if start or end are null
	 */
	public Collection<String> getWaypoints(String start, String end) {
		int st = _waypoints.indexOf(start.toUpperCase());
		int ed = _waypoints.indexOf(end.toUpperCase());
		if ((st == -1) || (ed == -1)) {
			return new ArrayList<String>();
		} else if (ed < st) {
			// If ed is before sd then reverse the waypoints
			List<String> wp2 = new ArrayList<String>(_waypoints.subList(ed, st));
			Collections.reverse(wp2);
			return wp2;
		}

		return _waypoints.subList(st, ed);
	}

	/**
	 * Returns the Airway route.
	 * @return a space-delimited list of waypoint codes
	 */
	public String getRoute() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<String> i = _waypoints.iterator(); i.hasNext();) {
			buf.append(i.next());
			if (i.hasNext())
				buf.append(' ');
		}

		return buf.toString();
	}

	/**
	 * Adds a waypoint to the Airway route.
	 * @param code the waypoint code
	 * @see Airway#getWaypoints()
	 */
	public void addWaypoint(String code) {
		if (code == null)
			return;

		code = code.trim().toUpperCase();
		if (!_waypoints.contains(code))
			_waypoints.add(code);
	}

	/**
	 * Updates the Airway code.
	 * @param code the code
	 * @throws NullPointerException if code is null
	 */
	public void setCode(String code) {
		_code = code.trim().toUpperCase();
	}

	/**
	 * Updates the Airway route. <i>The existing route will be cleared</i>.
	 * @param route a space-delimited list of waypoint codes
	 */
	public void setRoute(String route) {
		_waypoints.clear();
		StringTokenizer tkns = new StringTokenizer(route, " ");
		while (tkns.hasMoreTokens())
			addWaypoint(tkns.nextToken());
	}

	/**
	 * Compares two airways by comparing their names.
	 */
	public int compareTo(Object o) {
		Airway a2 = (Airway) o;
		return _code.compareTo(a2._code);
	}

	/**
	 * Returns the airway code.
	 */
	public String toString() {
		return getCode();
	}

	/**
	 * Returns the cache key.
	 * @return the airway/route code
	 */
	public Object cacheKey() {
		return getCode();
	}
}
// Copyright 2009, 2010, 2012, 2106 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.navdata.*;

/**
 * A Flight Route that has its waypoints populated.
 * @author Luke
 * @version 7.2
 * @since 2.6
 */

public class PopulatedRoute extends FlightRoute implements Route {
	
	private final Map<NavigationDataBean, String> _route = new LinkedHashMap<NavigationDataBean, String>(); 

	/**
	 * Helper method to copy a route from one PopulatedRoute to another.
	 * @param pr the PopulatedRoute bean
	 */
	protected void load(PopulatedRoute pr) {
		_route.putAll(pr._route);
	}
	
	/**
	 * Returns the waypoints on this route.
	 * @return a Collection of NavigationDataBeans
	 */
	@Override
	public LinkedList<NavigationDataBean> getWaypoints() {
		return new LinkedList<NavigationDataBean>(_route.keySet());
	}
	
	/**
	 * Returns the Airway each waypoint is on.
	 * @param nd the waypoint
	 * @return the Airway code, or null if none
	 */
	public String getAirway(NavigationDataBean nd) {
		return _route.get(nd);
	}
	
	/**
	 * Returns the number of waypoints.
	 */
	@Override
	public int getSize() {
		return _route.size();
	}
	
	/**
	 * Adds a waypoint to the route.
	 * @param nd the waypoint
	 * @param airway the airway it is on, or null
	 */
	public void addWaypoint(NavigationDataBean nd, String airway) {
		_route.put(nd, (airway == null) ? "" : airway);
	}
	
	/**
	 * Adds a waypoint to the route.
	 * @param nd the waypoint
	 */
	@Override
	public void addWaypoint(NavigationDataBean nd) {
		addWaypoint(nd, null);
	}
	
	/**
	 * Adds multiple waypoints to the route.
	 * @param nds a Collection of NavigationDataBeans
	 */
	public void addWaypoints(Collection<NavigationDataBean> nds) {
		for (NavigationDataBean nd: nds)
			addWaypoint(nd, null);
	}
	
	/**
	 * Adds a waypoint to the start of the route.
	 * @param nd the waypoint
	 * @param airway the airway it is on, or null
	 */
	public void insertWaypoint(NavigationDataBean nd, String airway) {
		Map<NavigationDataBean, String> tmpRoute = new LinkedHashMap<NavigationDataBean, String>();
		tmpRoute.put(nd, (airway == null) ? "" : airway);
		tmpRoute.putAll(_route);
		_route.clear();
		_route.putAll(tmpRoute);
	}
	
	/**
	 * Removes all waypoints on a particular Airway from the route.
	 * @param code the airway code
	 */
	public void removeAirway(String code) {
		for (Iterator<Map.Entry<NavigationDataBean, String>> i = _route.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<NavigationDataBean, String> me = i.next();
			if (me.getValue().equals(code))
				i.remove();
		}
	}
	
	/**
	 * Returns the route.
	 * @return a space-separated list of waypoints and airways
	 */
	@Override
	public String getRoute() {
		String rt = super.getRoute();
		if (rt != null)
			return rt;
		
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _route.keySet().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			buf.append(nd.getCode());
			if (i.hasNext())
				buf.append(' ');
		}
		
		return buf.toString();
	}

	@Override
	public String getComboAlias() {
		return getRoute();
	}

	@Override
	public String getComboName() {
		return toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
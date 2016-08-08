// Copyright 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

public class CacheableRoute implements Route {
	
	private final String _route;
	private final LinkedList<NavigationDataBean> _waypoints = new LinkedList<NavigationDataBean>();

	/**
	 * Creates the route.
	 * @param route the route text
	 * @param waypoints the waypoints
	 */
	public CacheableRoute(String route, Collection<NavigationDataBean> waypoints) {
		super();
		_route = route.toUpperCase();
		_waypoints.addAll(waypoints);
	}
	
	@Override
	public void addWaypoint(NavigationDataBean nd) {
		_waypoints.add(nd);
	}

	@Override
	public LinkedList<NavigationDataBean> getWaypoints() {
		return new LinkedList<NavigationDataBean>(_waypoints);
	}
	
	@Override
	public String getRoute() {
		return _route;
	}
	
	@Override
	public int getSize() {
		return _waypoints.size();
	}

	@Override
	public Object cacheKey() {
		return new Integer(_route.hashCode());
	}
	
	@Override
	public String toString() {
		return _route;
	}
	
	@Override
	public int hashCode() {
		return _route.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o != null) && (_route.equals(o.toString()));
	}
}
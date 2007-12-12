// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.LinkedList;

import org.deltava.beans.GeoLocation;
import org.deltava.util.cache.Cacheable;

/**
 * An interface to define the waypoints in a Route.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public interface Route extends Cacheable {

	/**
	 * Adds a waypoint to this route.
	 * @param code the waypoint code
	 * @param loc the waypoint location 
	 */
	public void addWaypoint(String code, GeoLocation loc);
	
	/**
	 * Returns the waypoints for this Route.
	 * @return a LinkedList of Intersections
	 */
	public LinkedList<NavigationDataBean> getWaypoints();
	
	/**
	 * Returns the number of Waypoints on this Route.
	 * @return the number of Waypoints
	 */
	public int getSize();
	
	/**
	 * Returns the Airway route.
	 * @return a space-delimited list of waypoint codes
	 */
	public String getRoute();
}
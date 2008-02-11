// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.LinkedList;

import org.deltava.util.cache.Cacheable;

/**
 * An interface to define the waypoints in a Route.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public interface Route extends Cacheable {

	/**
	 * Adds a waypoint to this route.
	 * @param nd the waypoint
	 */
	public void addWaypoint(NavigationDataBean nd);
	
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
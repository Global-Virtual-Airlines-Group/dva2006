// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.Collection;

import org.deltava.util.cache.Cacheable;

/**
 * An interface to define the waypoints in a Route.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Route extends Cacheable {

	/**
	 * Returns the waypoint IDs for this Route.
	 * @return a Collection of Waypoint IDs
	 */
	public Collection<String> getWaypoints();
	
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
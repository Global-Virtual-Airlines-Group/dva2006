// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Collection;

/**
 * An interface to allow ACARS implementations to return Connection Pool diagnostics and map entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSAdminInfo {

	/**
	 * Returns all current Connection data.
	 * @param showHidden TRUE if stealth connections should be displayed, otherwise FALSE
	 * @return a Collection of ACARSConnection beans
	 */
	public Collection getPoolInfo(boolean showHidden);

	/**
	 * Returns the positions of all ACARS flights.
	 * @return a Collection of RouteEntry beans
	 */
	public Collection<RouteEntry> getMapEntries();

	/**
	 * Returns the curent ACARS Flight IDs.
	 * @return a Collection of Integer flight IDs
	 */
	public Collection<Integer> getFlightIDs();

	/**
	 * Returns all live ACARS connection bans.
	 * @return a Collection of ban objects
	 */
	public Collection getBanInfo();

	/**
	 * Returns if there are any ACARS connections.
	 * @return TRUE if there is at least one connection, otherwise FALSE
	 */
	public boolean isEmpty();
}
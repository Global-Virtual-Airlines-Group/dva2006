// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object that loads trans-oceanic track NOTAMs.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public interface TrackDAO {

	/**
	 * Downloads track data.
	 * @return the track data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException;
	
	/**
	 * Returns the Waypoints for each Track.
	 * @return a Map of waypoint codes, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, Collection<String>> getWaypoints() throws DAOException;
}
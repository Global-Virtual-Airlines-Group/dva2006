// Copyright 2006, 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object that loads trans-oceanic track NOTAMs.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public abstract class TrackDAO extends DAO {
	
	/**
	 * Line separator
	 */
	protected static final String CRLF = System.getProperty("line.separator");

	/**
	 * Downloads track data.
	 * @return the track data
	 * @throws DAOException if an I/O error occurs
	 */
	public abstract String getTrackInfo() throws DAOException;
	
	/**
	 * Returns the Waypoints for each Track.
	 * @return a Map of waypoint codes, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	public abstract Map<String, Collection<String>> getWaypoints() throws DAOException;
}
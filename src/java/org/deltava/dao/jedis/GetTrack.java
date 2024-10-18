// Copyright 2016, 2018, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.jedis;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.DAOException;
import org.deltava.util.JedisUtils;

/**
 * A Data Access Object to get unpersisted ACARS track data from Jedis.
 * @author Luke
 * @version 11.3
 * @since 7.0
 */

public class GetTrack extends JedisDAO {

	/**
	 * Retrieves ACARS track data for a particular flight.
	 * @param isACARS TRUE if ACARS, FALSE if simFDR
	 * @param flightID the Flight ID
	 * @return a Collection of RouteEntry beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<GeoLocation> getTrack(boolean isACARS, String flightID) throws DAOException {
		setBucket("track", isACARS ? "acars" : "simFDR");
		try {
			@SuppressWarnings("unchecked")
			Collection<GeoLocation> results = (Collection<GeoLocation>) JedisUtils.get(createKey(flightID));
			return (results == null) ? new HashSet<GeoLocation>() : results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
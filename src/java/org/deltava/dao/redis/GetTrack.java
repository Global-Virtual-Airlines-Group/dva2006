// Copyright 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.DAOException;
import org.deltava.util.RedisUtils;

/**
 * A Data Access Object to get unpersisted ACARS track data from Redis.
 * @author Luke
 * @version 8.3
 * @since 7.0
 */

public class GetTrack extends RedisDAO {

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
			Collection<GeoLocation> results = (Collection<GeoLocation>) RedisUtils.get(createKey(flightID));
			return (results == null) ? new HashSet<GeoLocation>() : results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
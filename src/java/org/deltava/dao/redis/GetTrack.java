// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.DAOException;
import org.deltava.util.RedisUtils;

/**
 * A Data Access Object to get unpersisted ACARS track data from Redis.
 * @author Luke
 * @version 7.1
 * @since 7.0
 */

public class GetTrack extends RedisDAO {

	/**
	 * Retrieves ACARS track data for a particular flight.
	 * @param flightID the Flight ID
	 * @return a Collection of RouteEntry beans
	 */
	public Collection<GeoLocation> getTrack(int flightID) throws DAOException {
		setBucket("acarsTrack");
		
		try {
			@SuppressWarnings("unchecked")
			Collection<GeoLocation> results = (Collection<GeoLocation>) RedisUtils.get(createKey(String.valueOf(flightID)));
			return (results == null) ? new HashSet<GeoLocation>() : results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.dao.DAOException;
import org.deltava.util.MemcachedUtils;

/**
 * A Data Access Object to get unpersisted ACARS track data from memcached.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class GetTrack extends MemcachedDAO {

	/**
	 * Retrieves ACARS track data for a particular flight.
	 * @param flightID the Flight ID
	 * @return a Collection of RouteEntry beans
	 */
	public Collection<RouteEntry> getTrack(int flightID) throws DAOException {
		setBucket("acarsTrack");
		
		try {
			@SuppressWarnings("unchecked")
			Collection<RouteEntry> results = (Collection<RouteEntry>) MemcachedUtils.get(createKey(String.valueOf(flightID)), 150);
			return (results == null) ? new HashSet<RouteEntry>() : results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
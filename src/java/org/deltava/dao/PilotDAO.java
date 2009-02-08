// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.beans.Person;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to support reading and writing Pilot object(s) to the database. This DAO contains the shared
 * Pilot cache which other DAOs which access the PILOTS table may access.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

abstract class PilotDAO extends DAO implements CachingDAO {
	
	/**
	 * The Pilot bean cache.
	 */
	protected static final Cache<Person> _cache = new ExpiringCache<Person>(512, 3600);

	/**
	 * Initializes the Data Access Object. 
	 * @param c the JDBC connection to use
	 */
	protected PilotDAO(Connection c) {
		super(c);
	}

	/**
	 * Removes an entry from the pilot cache.
	 * @param id the database ID, or -1 to invalidate the entire cache
	 */
	static void invalidate(int id) {
		if (id == -1)
			_cache.clear();
		else
			_cache.remove(new Integer(id));
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _cache.getHits();
	}
}
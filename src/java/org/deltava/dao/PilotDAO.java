// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.beans.Person;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to support reading and writing Pilot object(s) to the database. This DAO contains the shared
 * Pilot cache which other DAOs which access the PILOTS table may access.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

abstract class PilotDAO extends DAO implements CachingDAO {
	
	/**
	 * The Pilot bean cache.
	 */
	protected static final Cache<Person> _cache = new ExpiringCache<Person>(128, 2400);

	/**
	 * Initializes the Data Access Object. 
	 * @param c the JDBC connection to use
	 */
	protected PilotDAO(Connection c) {
		super(c);
	}
	
	/**
	 * Removes an entry from the pilot cache.
	 * @param id the database ID
	 * @see PilotDAO#invalidate(Cacheable)
	 */
	static void invalidate(int id) {
		Integer key = new Integer(id);
		_cache.remove(key);
	}
	
	/**
	 * Removes an entry from the cache.
	 * @param obj the entry
	 * @see PilotDAO#invalidate(int)
	 */
	static void invalidate(Cacheable obj) {
		_cache.remove(obj.cacheKey());
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
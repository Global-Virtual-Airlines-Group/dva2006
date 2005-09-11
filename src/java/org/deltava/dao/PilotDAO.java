// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to support reading and writing Pilot object(s) to the database. This DAO contains the shared
 * Pilot cache which other DAOs which access the PILOTS table may access.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

abstract class PilotDAO extends DAO {
	
	/**
	 * The Pilot bean cache.
	 */
	protected static final Cache _cache = new ExpiringCache(128, 1800);

	/**
	 * Initializes the Data Access Object. 
	 * @param c the JDBC connection to use
	 */
	protected PilotDAO(Connection c) {
		super(c);
	}
}
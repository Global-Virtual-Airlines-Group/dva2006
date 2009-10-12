// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.util.cache.*;

/**
 * A Data Access Object used to cache signature image data. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public abstract class PilotSignatureDAO extends DAO implements CachingDAO {
	
	protected static final Cache<CacheableLong> _sigCache = new AgingCache<CacheableLong>(1024);

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected PilotSignatureDAO(Connection c) {
		super(c);
	}

	/**
	 * Invalidates a Pilot's cache entry, and invalidates the Pilot DAO cache entry for the Pilot.
	 * @param id the Pilot's database ID
	 * @see PilotDAO#invalidate(int)
	 */
	protected void invalidate(int id) {
		_sigCache.remove(Integer.valueOf(id));
		PilotDAO.invalidate(id);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_sigCache);
	}
}
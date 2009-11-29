// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.beans.wx.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object for Weather data. 
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

abstract class WeatherDAO extends DAO implements CachingDAO {
	
	protected static final Cache<METAR> _wxCache = new ExpiringCache<METAR>(1024, 3600);
	protected static final Cache<TAF> _tafCache = new ExpiringCache<TAF>(1024, 3600);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected WeatherDAO(Connection c) {
		super(c);
	}

	@Override
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_wxCache);
		info.add(_tafCache);
		return info;
	}
}
// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;

import org.deltava.beans.EquipmentType;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for {@link EquipmentType} beans
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public abstract class EquipmentTypeDAO extends DAO implements CachingDAO {
	
	protected static final Cache<EquipmentType> _cache = new AgingCache<EquipmentType>(10);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected EquipmentTypeDAO(Connection c) {
		super(c);
	}

	@Override
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}

	/**
	 * Invalidates a cache entry for the current database.
	 * @param eqType the equipment type to invalidate
	 * @see EquipmentType#cacheKey()
	 */
	protected void invalidate(String eqType) {
		_cache.remove(SystemData.getObject("airline.db") + "!!" + eqType);
	}
}
// Copyright 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.ipc;

import java.util.*;

import org.deltava.beans.acars.ACARSMapEntry;

import org.deltava.dao.CachingDAO;

import org.deltava.util.IPCUtils;
import org.deltava.util.cache.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Data Access Object to cache IPC calls for ACARS Connection Pool data. 
 * @author Luke
 * @version 3.0
 * @since 2.3
 */

public class GetACARSPool implements CachingDAO {
	
	private static final Cache<CacheableCollection<ACARSMapEntry>> _cache = 
		new ExpiringCache<CacheableCollection<ACARSMapEntry>>(1, 3);
	private static final Cache<CacheableCollection<Integer>> _idCache =
		new ExpiringCache<CacheableCollection<Integer>>(1, 3);

	/**
	 * Initializes the Data Access Object. 
	 */
	public GetACARSPool() {
		super();
	}
	
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_cache);
		info.add(_idCache);
		return info;
	}

	/**
	 * Returns ACARS Map entries.
	 * @return a Collection of Map entries
	 */
	public Collection<ACARSMapEntry> getEntries() {
		Collection<ACARSMapEntry> entries = _cache.get(GetACARSPool.class);
		if (entries != null)
			return entries;
		
		// Reload the caches
		try {
			reload();
			return _cache.get(GetACARSPool.class);
		} catch (IllegalStateException ise) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Returns active ACARS Flight IDs.
	 * @return a Collection of Flight IDs
	 */
	public Collection<Integer> getFlightIDs() {
		CacheableCollection<Integer> ids = _idCache.get(GetACARSPool.class);
		if (ids != null)
			return ids;
		
		// Reload the caches
		try {
			reload();
			return _idCache.get(GetACARSPool.class).clone();
		} catch (NullPointerException npe) { 
			return Collections.emptyList();
		} catch (IllegalStateException ise) {
			return Collections.emptyList();
		}
	}

	/**
	 * Helper method to reload the caches.
	 */
	@SuppressWarnings("unchecked")
	private synchronized void reload() throws IllegalStateException {
		
		// Get the connection pool
		ACARSAdminInfo<ACARSMapEntry> acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
		if (acarsPool == null)
			throw new IllegalStateException("No ACARS Connection Pool");

		// Repopulate the caches
		CacheableCollection<ACARSMapEntry> entries = new CacheableList<ACARSMapEntry>(GetACARSPool.class);
		CacheableCollection<Integer> ids = new CacheableSet<Integer>(GetACARSPool.class);
		entries.addAll(IPCUtils.deserialize(acarsPool));
		ids.addAll(acarsPool.getFlightIDs());
		_cache.add(entries);
		_idCache.add(ids);
	}
}
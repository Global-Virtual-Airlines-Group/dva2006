// Copyright 2008, 2009, 2010, 2012, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.ipc;

import java.util.*;

import org.deltava.beans.acars.ACARSMapEntry;

import org.deltava.util.IPCUtils;
import org.deltava.util.cache.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Data Access Object to cache IPC calls for ACARS Connection Pool data. 
 * @author Luke
 * @version 8.3
 * @since 2.3
 */

@SuppressWarnings("static-method")
public class GetACARSPool {
	
	private static final Cache<CacheableCollection<ACARSMapEntry>> _cache = CacheManager.getCollection(ACARSMapEntry.class, "ACARSMap");
	private static final Cache<CacheableCollection<Integer>> _idCache = CacheManager.getCollection(Integer.class, "ACARSFlightID");

	/**
	 * Returns ACARS Map entries.
	 * @return a Collection of Map entries
	 */
	public Collection<ACARSMapEntry> getEntries() {
		CacheableCollection<ACARSMapEntry> entries = _cache.get(GetACARSPool.class);
		if (entries != null)
			return entries.clone();
		
		try {
			reload();
			return _cache.get(GetACARSPool.class).clone();
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
		if (ids != null) return ids.clone();
		
		try {
			reload();
			return _idCache.get(GetACARSPool.class).clone();
		} catch (NullPointerException | IllegalStateException e) { 
			return Collections.emptyList();
		}
	}

	/*
	 * Helper method to reload the caches.
	 */
	@SuppressWarnings("unchecked")
	private synchronized static void reload() throws IllegalStateException {
		
		// Get the connection pool
		ACARSAdminInfo<ACARSMapEntry> acarsPool = (ACARSAdminInfo<ACARSMapEntry>) SharedData.get(SharedData.ACARS_POOL);
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
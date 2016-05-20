// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.util.MemcachedUtils;

import net.spy.memcached.*;

/**
 * A Data Access Object to save temporary ACARS track data to memcached.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SetTrack extends MemcachedDAO {
	
	private static final Logger log = Logger.getLogger(SetTrack.class);
	private static boolean _logStack = true;

	/**
	 * Adds a route entry to memcached.
	 * @param entry a RouteEntry
	 */
	public void write(RouteEntry entry) {
		setBucket("acarsTrack");
		
		CASMutation<Object> mutation = new CASMutation<Object>() {
	    	@Override
			public Collection<RouteEntry> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
	            Collection<RouteEntry> c2 = new TreeSet<RouteEntry>((Collection<RouteEntry>) current);
	            c2.add(entry);
	            return c2;
	        }
	    };
	    
	    try {
	    	CASMutator<Object> m = MemcachedUtils.getMutator();
	    	m.cas(createKey(String.valueOf(entry.getID())), Collections.singletonList(entry), 1800, mutation);
	    	_logStack = true;
	    } catch (Exception e) {
	    	if (_logStack) {
	    		log.error(e.getMessage(), e);
	    		_logStack = false;
	    	} else
	    		log.warn(e.getMessage());
	    }
	}

	/**
	 * Deletes a track from the cache.
	 * @param flightID the Flight ID
	 */
	public void clear(int flightID) {
		setBucket("acarsTrack");
		MemcachedUtils.delete(createKey(String.valueOf(flightID)));
	}
}
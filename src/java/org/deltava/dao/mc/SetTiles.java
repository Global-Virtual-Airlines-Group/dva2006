// Copyright 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.time.Instant;
import java.util.*;

import net.spy.memcached.*;

import org.deltava.dao.DAOException;
import org.deltava.util.MemcachedUtils;
import org.deltava.util.tile.SeriesWriter;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to write map tiles to memcached. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class SetTiles extends MemcachedDAO implements SeriesWriter {

	/**
	 * Writes an ImageSeries to memcached. 
	 * @param is the ImageSeries
	 * @throws DAOException if an error occured
	 */
	@Override
	public void write(ImageSeries is) throws DAOException {
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().toEpochMilli());
		
		addImageType(is.getType());
		addImageDate(is.getType(), is.getDate());
		
		// Write the Tiles
		setBucket("mapTiles", is.getType(), seriesDate);
		MemcachedUtils.write(createKey("$ME"), _expiry, Boolean.TRUE);
		MemcachedUtils.write(createKey("$SIZE"), _expiry, Integer.valueOf(is.size()));
		MemcachedUtils.write(createKey("$KEYS"), _expiry, new ArrayList<TileAddress>(is.keySet()));
		is.entrySet().forEach(me -> MemcachedUtils.write(createKey(me.getKey().getName()), _expiry, me.getValue()));
	}
	
	/**
	 * Purges an image series from memcached.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occured
	 */
	public void purge(ImageSeries is) throws DAOException {
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().toEpochMilli());
		try {
			setBucket("mapTiles", is.getType(), seriesDate);
			boolean hasSeries = (MemcachedUtils.get(createKey("$ME"), 1000) != null);
			if (!hasSeries) return;
		
			@SuppressWarnings("unchecked")
			Collection<TileAddress> keys = (Collection<TileAddress>) MemcachedUtils.get(createKey("$KEYS"), 1000);
			if (keys != null)
				keys.forEach(k -> MemcachedUtils.delete(createKey(k.getName())));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/*
	 * Helper method to use CAS to add an image type.
	 */
	private void addImageType(final String type) throws DAOException {
		setBucket("mapTiles");
		
		CASMutation<Object> mutation = new CASMutation<Object>() {
	    	@Override
			public Collection<String> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
	    		Collection<String> c = (Collection<String>) current;
	            Collection<String> c2 = new TreeSet<String>(c);
	            c2.add(type);
	            return c2;
	        }
	    };

	    try {
	    	CASMutator<Object> m = MemcachedUtils.getMutator();
	    	m.cas(createKey("types"), Collections.singletonList(type), 86400, mutation);
	    } catch (Exception e) {
	    	throw new DAOException(e);
	    }
	}

	/*
	 * Helper method to use CAS to add an imagery effective date.
	 */
	private void addImageDate(String type, final Instant effDate) throws DAOException {
		if (effDate == null) return;
		setBucket("mapTiles", type);

		CASMutation<Object> mutation = new CASMutation<Object>() {
	    	@Override
			public List<Instant> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
				Collection<Instant> c = (Collection<Instant>) current;
	            List<Instant> ll = new ArrayList<Instant>(c);
	            while (ll.size() > 10)
	                ll.remove(0);

	            ll.add(effDate);
	            return ll;
	        }
	    };

	    try {
	    	CASMutator<Object> m = MemcachedUtils.getMutator();
	    	m.cas(createKey("dates"), Collections.singletonList(effDate), 3600, mutation);
	    } catch (Exception e) {
	    	throw new DAOException(e);
	    }
	}
}
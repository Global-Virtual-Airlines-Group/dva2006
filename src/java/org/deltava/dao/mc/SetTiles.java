// Copyright 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import net.spy.memcached.*;

import org.deltava.dao.DAOException;
import org.deltava.util.tile.*;

/**
 * A Data Access Object to write tiles to memcached. 
 * @author Luke
 * @version 5.2
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
		checkConnection();
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().getTime());
		
		addImageType(is.getType());
		addImageDate(is.getType(), is.getDate());
		
		// Write the Tiles
		setBucket("mapTiles", is.getType(), seriesDate);
		_client.add(createKey("$ME"), _expiry, Boolean.TRUE);
		_client.add(createKey("$SIZE"), _expiry, Integer.valueOf(is.size()));
		_client.add(createKey("$KEYS"), _expiry, new ArrayList<TileAddress>(is.keySet()));
		for (Map.Entry<TileAddress, PNGTile> me : is.entrySet()) {
			String key = createKey(me.getKey().getName());
			_client.add(key, _expiry, me.getValue());
		}
	}
	
	/**
	 * Purges an image series from memcached.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occured
	 */
	public void purge(ImageSeries is) throws DAOException {
		checkConnection();
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().getTime());
		
		setBucket("mapTiles", is.getType(), seriesDate);
		boolean hasSeries = (_client.get(createKey("$ME")) != null);
		if (!hasSeries) return;
		
		@SuppressWarnings("unchecked")
		Collection<TileAddress> keys = (Collection<TileAddress>) _client.get(createKey("$KEYS"));
		if (keys != null) {
			for (TileAddress addr : keys) {
				String k = createKey(addr.getName());
				_client.delete(k);
			}
		}
	}
	
	/*
	 * Helper method to use CAS to add an image type.
	 */
	private void addImageType(final String type) throws DAOException {
		setBucket("mapTiles");
		
		CASMutation<Object> mutation = new CASMutation<Object>() {
	    	public Collection<String> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
	    		Collection<String> c = (Collection<String>) current;
	            Collection<String> c2 = new TreeSet<String>(c);
	            c2.add(type);
	            return c2;
	        }
	    };

	    try {
	    	CASMutator<Object> m = new CASMutator<Object>(_client, _client.getTranscoder());
	    	m.cas(createKey("types"), Collections.singletonList(type), 86400, mutation);
	    } catch (Exception e) {
	    	throw new DAOException(e);
	    }
	}

	/*
	 * Helper method to use CAS to add an imagery effective date.
	 */
	private void addImageDate(String type, final Date effDate) throws DAOException {
		if (effDate == null) return;
		setBucket("mapTiles", type);

		CASMutation<Object> mutation = new CASMutation<Object>() {
	    	public List<Date> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
				Collection<Date> c = (Collection<Date>) current;
	            List<Date> ll = new ArrayList<Date>(c);
	            while (ll.size() > 10)
	                ll.remove(0);

	            ll.add(effDate);
	            return ll;
	        }
	    };

	    try {
	    	CASMutator<Object> m = new CASMutator<Object>(_client, _client.getTranscoder());
	    	m.cas(createKey("dates"), Collections.singletonList(effDate), 3600, mutation);
	    } catch (Exception e) {
	    	throw new DAOException(e);
	    }
	}
}
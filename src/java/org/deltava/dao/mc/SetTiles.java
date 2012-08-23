// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import net.spy.memcached.*;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;

/**
 * A Data Access Object to write tiles to memcached. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class SetTiles extends MemcachedDAO implements SeriesWriter {

	/**
	 * Writes an ImageSeries to memcached. 
	 * @param is the ImageSeries
	 * @throws DAOException
	 */
	@Override
	public void write(ImageSeries is) throws DAOException {
		checkConnection();
		
		addImageType(is.getType());
		addImageDate(is.getType(), is.getDate());
		
		// Write the Tiles
		setBucket("wuTiles", is.getType(), String.valueOf(is.getDate().getTime()));
		_client.add(createKey("$ME"), _expiry, Boolean.TRUE);
		_client.add(createKey("$SIZE"), _expiry, Integer.valueOf(is.size()));	
		for (PNGTile pt : is) {
			String key = createKey(pt.getAddress().getName());
			_client.add(key, _expiry, pt);
		}
	}
	
	/*
	 * Helper method to use CAS to add an image type.
	 */
	private void addImageType(final String type) throws DAOException {
		setBucket("wuTiles");
		
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
		setBucket("wuTiles", type);

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
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
		
		// Write the Tiles
		setBucket("wuTiles", is.getType(), String.valueOf(is.getDate().getTime()));
		for (PNGTile pt : is) {
			String key = createKey(pt.getAddress());
			_client.add(key, _expiry, pt);
		}
		
		// Change the bucket
		setBucket("wuTiles", is.getType());
		
	    // This is how we modify a list when we find one in the cache.
		final Date effDate = is.getDate();
	    CASMutation<Object> mutation = new CASMutation<Object>() {
	    	public List<Date> getNewValue(Object current) {
	    		@SuppressWarnings("unchecked")
				List<Date> c = (List<Date>) current;
	            List<Date> ll = new ArrayList<Date>(c);
	            if(ll.size() > 10)
	                ll.remove(0);

	            ll.add(effDate);
	            return ll;
	        }
	    };

	    // Store the effective date using CAS
	    try {
	    	CASMutator<Object> m = new CASMutator<Object>(_client, _client.getTranscoder());
	    	m.cas(createKey("dates"), Collections.singletonList(effDate), 180, mutation);
	    } catch (Exception e) {
	    	throw new DAOException(e);
	    }
	}
}
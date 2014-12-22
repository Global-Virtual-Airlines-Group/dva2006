// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;
import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to write wind data to memcached.
 * @author Luke
 * @version 5.4
 * @since 5.4
 */

public class SetWinds extends MemcachedDAO {

	/**
	 * Writes wind data to memcached.
	 * @param data the WindData
	 * @throws DAOException if an error occured
	 */
	public void write(java.util.Collection<WindData> data) throws DAOException {
		checkConnection();

		// Bucketize into seperate collections per pressure level
		Map<PressureLevel, Collection<WindData>> sd = new TreeMap<PressureLevel, Collection<WindData>>();
		data.forEach(wd -> CollectionUtils.addMapCollection(sd, wd.getLevel(), wd));
		
		// Write each level
		for (Map.Entry<PressureLevel, Collection<WindData>> me : sd.entrySet()) {
			setBucket("winds", me.getKey().toString());
			Collection<WindData> wd = me.getValue();
			Collection<Object> keys = new ArrayList<Object>();
			_client.add(createKey("$ME"), _expiry, Boolean.TRUE);
			for (WindData w : wd) {
				Object key = w.cacheKey();
				_client.add(createKey(key), _expiry, w);
				keys.add(key);
			}
			
			_client.add(createKey("$KEYS"), _expiry, keys);
			_client.add(createKey("$SIZE"), _expiry, Integer.valueOf(keys.size()));
		}
	}
}
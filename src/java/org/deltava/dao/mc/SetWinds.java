// Copyright 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;
import org.deltava.util.CollectionUtils;
import org.deltava.util.MemcachedUtils;

/**
 * A Data Access Object to write wind data to memcached.
 * @author Luke
 * @version 6.1
 * @since 5.4
 */

public class SetWinds extends MemcachedDAO {

	/**
	 * Writes wind data to memcached.
	 * @param data the WindData
	 * @throws DAOException if an error occured
	 */
	public void write(java.util.Collection<WindData> data) throws DAOException {

		// Bucketize into seperate collections per pressure level
		Map<PressureLevel, Collection<WindData>> sd = new TreeMap<PressureLevel, Collection<WindData>>();
		data.forEach(wd -> CollectionUtils.addMapCollection(sd, wd.getLevel(), wd));
		
		// Write each level
		for (Map.Entry<PressureLevel, Collection<WindData>> me : sd.entrySet()) {
			setBucket("winds", me.getKey().toString());
			Collection<WindData> wd = me.getValue();
			Collection<Object> keys = new ArrayList<Object>();
			MemcachedUtils.write(createKey("$ME"), _expiry, Boolean.TRUE);
			for (WindData w : wd) {
				Object key = w.cacheKey();
				MemcachedUtils.write(createKey(key), _expiry, w);
				keys.add(key);
			}
			
			MemcachedUtils.write(createKey("$KEYS"), _expiry, keys);
			MemcachedUtils.write(createKey("$SIZE"), _expiry, Integer.valueOf(keys.size()));
		}
	}
}
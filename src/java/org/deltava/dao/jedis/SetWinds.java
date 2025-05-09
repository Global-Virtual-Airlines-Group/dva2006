// Copyright 2014, 2015, 2016, 2021, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.jedis;

import java.util.*;

import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

/**
 * A Data Access Object to write wind data to Redis.
 * @author Luke
 * @version 11.6
 * @since 5.4
 */

public class SetWinds extends JedisDAO {

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
			JedisUtils.write(createKey("$ME"), _expiry, Boolean.TRUE);
			JedisUtils.write("$KEYS", _expiry, keys);
			JedisUtils.write("$SIZE", _expiry, Integer.valueOf(keys.size()));
			try (Jedis j = SystemData.getJedisPool().getConnection()) {
				Pipeline jp = j.pipelined();
				for (WindData w : wd) {
					byte[] key = JedisUtils.encodeKey(createKey(w.cacheKey()));
					j.set(key, JedisUtils.write(w), SetParams.setParams().ex(_expiry));
					keys.add(key);
				}
				
				jp.sync();
			} catch (Exception e) {
				throw new DAOException(e);
			}
		}
	}
}
// Copyright 2014, 2015, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;

import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;
import org.deltava.util.*;

import redis.clients.jedis.*;

/**
 * A Data Access Object to write wind data to Redis.
 * @author Luke
 * @version 10.0
 * @since 5.4
 */

public class SetWinds extends RedisDAO {

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
			RedisUtils.write(createKey("$ME"), _expiry, Boolean.TRUE);
			try (Jedis j = RedisUtils.getConnection()) {
				Pipeline jp = j.pipelined();
				for (WindData w : wd) {
					byte[] key = RedisUtils.encodeKey(createKey(w.cacheKey()));
					j.set(key, RedisUtils.write(w));
					j.expireAt(key, _expiry);
					keys.add(key);
				}
				
				jp.sync();
				RedisUtils.write("$KEYS", _expiry, keys);
				RedisUtils.write("$SIZE", _expiry, Integer.valueOf(keys.size()));
			}
		}
	}
}
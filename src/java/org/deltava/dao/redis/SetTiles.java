// Copyright 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.time.Instant;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.util.RedisUtils;
import org.deltava.util.tile.SeriesWriter;

import org.gvagroup.tile.*;

import redis.clients.jedis.Jedis;

/**
 * A Data Access Object to write map tiles to Redis. 
 * @author Luke
 * @version 7.1
 * @since 5.0
 */

public class SetTiles extends RedisDAO implements SeriesWriter {

	/**
	 * Writes an ImageSeries to Redis. 
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
		try (Jedis j = RedisUtils.getConnection()) {
			j.pipelined();
			j.setex(RedisUtils.encodeKey(createKey("$ME")), _expiry, RedisUtils.write(Boolean.TRUE));
			j.setex(RedisUtils.encodeKey(createKey("$SIZE")), _expiry, RedisUtils.write(Integer.valueOf(is.size())));
			j.setex(RedisUtils.encodeKey(createKey("$ME")), _expiry, RedisUtils.write(new ArrayList<TileAddress>(is.keySet())));
			is.entrySet().forEach(me -> j.setex(RedisUtils.encodeKey(createKey(me.getKey().getName())), _expiry, RedisUtils.write(me.getValue())));
			j.sync();
		}
	}
	
	/**
	 * Purges an image series from Redis.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occured
	 */
	public void purge(ImageSeries is) throws DAOException {
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().toEpochMilli());
		try {
			setBucket("mapTiles", is.getType(), seriesDate);
			boolean hasSeries = (RedisUtils.get(createKey("$ME")) != null);
			if (!hasSeries) return;
		
			@SuppressWarnings("unchecked")
			Collection<TileAddress> keys = (Collection<TileAddress>) RedisUtils.get(createKey("$KEYS"));
			if (keys != null) {
				try (Jedis j = RedisUtils.getConnection()) {
					j.pipelined();
					j.expire(RedisUtils.encodeKey(createKey("$SIZE")), 0);
					j.expire(RedisUtils.encodeKey(createKey("$KEYS")), 0);
					j.expire(RedisUtils.encodeKey(createKey("$ME")), 0);
					keys.forEach(k -> j.expire(RedisUtils.encodeKey(createKey(k.getName())), 0));
					j.sync();
				}
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/*
	 * Helper method to use CAS to add an image type.
	 */
	private void addImageType(String type) {
		setBucket("mapTiles");
		RedisUtils.push(createKey("types"), type, 0);
	}

	/*
	 * Helper method to use CAS to add an imagery effective date.
	 */
	private void addImageDate(String type, Instant effDate) {
		if (effDate == null) return;
		setBucket("mapTiles", type);
		RedisUtils.push(createKey("dates"), String.valueOf(effDate.toEpochMilli()), 10);
	}
}
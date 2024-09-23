// Copyright 2012, 2013, 2015, 2016, 2021, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;
import java.time.Instant;

import redis.clients.jedis.*;

import org.deltava.dao.DAOException;

import org.deltava.util.RedisUtils;
import org.deltava.util.tile.*;

/**
 * A Data Access Object to write map tiles to Redis. 
 * @author Luke
 * @version 11.3
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
			RedisUtils.write("$ME", _expiry, Boolean.TRUE);
			RedisUtils.write("$SIZE", _expiry, Integer.valueOf(is.size()));
			RedisUtils.write("$ME", _expiry, new ArrayList<TileAddress>(is.keySet()));
			Pipeline jp = j.pipelined();
			for (Map.Entry<TileAddress, PNGTile> me : is.entrySet()) {
				byte[] k = RedisUtils.encodeKey(createKey(me.getKey().getName()));
				j.set(k, me.getValue().getData());
				j.expireAt(k, _expiry);
			}
			
			jp.sync();
		}
	}
	
	/**
	 * Purges an image series from Redis.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occured
	 */
	@Override
	public void purge(ImageSeries is) throws DAOException {
		Long seriesDate = (is.getDate() == null) ? null : Long.valueOf(is.getDate().toEpochMilli());
		try {
			setBucket("mapTiles", is.getType(), seriesDate);
			boolean hasSeries = (RedisUtils.get(createKey("$ME")) != null);
			if (!hasSeries) return;
		
			Collection<?> keys = (Collection<?>) RedisUtils.get(createKey("$KEYS"));
			if (keys != null) {
				try (Jedis j = RedisUtils.getConnection()) {
					Pipeline jp = j.pipelined();
					j.del(RedisUtils.encodeKey(createKey("$SIZE")));
					j.del(RedisUtils.encodeKey(createKey("$KEYS")));
					j.del(RedisUtils.encodeKey(createKey("$ME")));
					keys.forEach(k -> j.del(RedisUtils.encodeKey(createKey(((TileAddress)k).getName()))));
					jp.sync();
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
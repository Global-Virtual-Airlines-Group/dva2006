// Copyright 2012, 2013, 2015, 2016, 2021, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.jedis;

import java.util.*;
import java.time.Instant;

import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

import org.deltava.dao.DAOException;

import org.deltava.util.JedisUtils;
import org.deltava.util.system.SystemData;
import org.deltava.util.tile.*;

/**
 * A Data Access Object to write map tiles to Jedis. 
 * @author Luke
 * @version 11.6
 * @since 5.0
 */

public class SetTiles extends JedisDAO implements SeriesWriter {

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
		JedisUtils.write("$ME", _expiry, Boolean.TRUE);
		JedisUtils.write("$SIZE", _expiry, Integer.valueOf(is.size()));
		JedisUtils.write("$ME", _expiry, new ArrayList<TileAddress>(is.keySet()));
		try (Jedis j = SystemData.getJedisPool().getConnection()) {
			Pipeline jp = j.pipelined();
			for (Map.Entry<TileAddress, PNGTile> me : is.entrySet()) {
				byte[] k = JedisUtils.encodeKey(createKey(me.getKey().getName()));
				j.set(k, me.getValue().getData(), SetParams.setParams().ex(_expiry));
			}
			
			jp.sync();
		} catch (Exception e) {
			throw new DAOException(e);
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
			boolean hasSeries = (JedisUtils.get(createKey("$ME")) != null);
			if (!hasSeries) return;
		
			Collection<?> keys = (Collection<?>) JedisUtils.get(createKey("$KEYS"));
			if (keys != null) {
				try (Jedis j = SystemData.getJedisPool().getConnection()) {
					Pipeline jp = j.pipelined();
					j.del(JedisUtils.encodeKey(createKey("$SIZE")));
					j.del(JedisUtils.encodeKey(createKey("$KEYS")));
					j.del(JedisUtils.encodeKey(createKey("$ME")));
					keys.forEach(k -> j.del(JedisUtils.encodeKey(createKey(((TileAddress)k).getName()))));
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
		JedisUtils.push(createKey("types"), type, 0);
	}

	/*
	 * Helper method to use CAS to add an imagery effective date.
	 */
	private void addImageDate(String type, Instant effDate) {
		if (effDate == null) return;
		setBucket("mapTiles", type);
		JedisUtils.push(createKey("dates"), String.valueOf(effDate.toEpochMilli()), 10);
	}
}
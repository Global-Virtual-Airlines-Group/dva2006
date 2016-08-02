// Copyright 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.time.Instant;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.util.RedisUtils;
import org.deltava.util.tile.SeriesWriter;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to write map tiles to Redis. 
 * @author Luke
 * @version 7.1
 * @since 5.0
 */

public class SetTiles extends RedisDAO implements SeriesWriter {

	/**
	 * Writes an ImageSeries to memcached. 
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
		RedisUtils.write(createKey("$ME"), _expiry, Boolean.TRUE);
		RedisUtils.write(createKey("$SIZE"), _expiry, Integer.valueOf(is.size()));
		RedisUtils.write(createKey("$KEYS"), _expiry, new ArrayList<TileAddress>(is.keySet()));
		is.entrySet().forEach(me -> RedisUtils.write(createKey(me.getKey().getName()), _expiry, me.getValue()));
	}
	
	/**
	 * Purges an image series from memcached.
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
			if (keys != null)
				keys.forEach(k -> RedisUtils.delete(createKey(k.getName())));
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
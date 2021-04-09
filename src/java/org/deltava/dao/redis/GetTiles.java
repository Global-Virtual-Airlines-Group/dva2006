// Copyright 2012, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

import org.deltava.dao.DAOException;

import org.deltava.util.RedisUtils;
import org.deltava.util.tile.SeriesReader;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to read tiles from Redis. 
 * @author Luke
 * @version 10.0
 * @since 5.0
 */

public class GetTiles extends RedisDAO implements SeriesReader {

	@Override
	public Collection<String> getTypes() throws DAOException {
		setBucket("mapTiles");
		try {
			Collection<?> results = (Collection<?>) RedisUtils.get(createKey("types"));
			return (results == null) ? Collections.emptySet() : results.stream().map(String::valueOf).collect(Collectors.toSet());
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	@Override
	public Collection<Instant> getDates(String type) throws DAOException {
		setBucket("mapTiles", type);
		try {
			Collection<?> rawDates = (Collection<?>) RedisUtils.get(createKey("dates"));
			if (rawDates == null)
				return Collections.emptySet();
			
			// Validate that the tiles exist
			Collection<Instant> dates = new TreeSet<Instant>();
			for (Object dt : rawDates) {
				setBucket("mapTiles", type, dt);
				try {
					Object o = RedisUtils.get(createKey("$ME"));
					if (o != null) dates.add(Instant.ofEpochMilli(Long.parseLong(String.valueOf(dt))));
				} catch (Exception e) {
					// empty
				}
			}
			
			return dates;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	@Override
	public PNGTile getTile(String imgType, Instant effDate, TileAddress addr) throws DAOException {
		setBucket("mapTiles", imgType, (effDate == null) ? null : String.valueOf(effDate.toEpochMilli()));
		try {
			return (PNGTile) RedisUtils.get(createKey(addr.getName()));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
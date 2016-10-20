// Copyright 2012, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.redis;

import java.util.*;
import java.time.Instant;

import org.deltava.dao.DAOException;

import org.deltava.util.RedisUtils;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to read tiles from Redis. 
 * @author Luke
 * @version 7.2
 * @since 5.0
 */

public class GetTiles extends RedisDAO {

	/**
	 * Lists the available imagery types.
	 * @return a Collection of types
	 * @throws DAOException if an error occurs
	 */
	public Collection<String> getTypes() throws DAOException {
		setBucket("mapTiles");
		try {
			@SuppressWarnings("unchecked")
			Collection<String> results = (Collection<String>) RedisUtils.get(createKey("types"));
			return (results == null) ? new HashSet<String>() : results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Reads available image dates from memcached. 
	 * @param type the image type
	 * @return a Collection of Dates
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public Collection<Instant> getDates(String type) throws DAOException {
		setBucket("mapTiles", type);
		try {
			@SuppressWarnings("unchecked")
			Collection<String> rawDates = (Collection<String>) RedisUtils.get(createKey("dates"));
			if (rawDates == null)
				return new HashSet<Instant>();
			
			// Validate that the tiles exist
			Collection<Instant> dates = new TreeSet<Instant>();
			for (String dt : rawDates) {
				setBucket("mapTiles", type, dt);
				try {
					Object o = RedisUtils.get(createKey("$ME"));
					if (o != null) dates.add(Instant.ofEpochMilli(Long.parseLong(dt)));
				} catch (Exception e) {
					// empty
				}
			}
			
			return dates;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Reads a tile from cache.
	 * @param imgType the image type
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public PNGTile getTile(String imgType, TileAddress addr) throws DAOException {
		return getTile(imgType, null, addr);
	}
	
	/**
	 * Reads a tile from cache.
	 * @param imgType the image type
	 * @param effDate the effective date
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public PNGTile getTile(String imgType, Instant effDate, TileAddress addr) throws DAOException {
		setBucket("mapTiles", imgType, (effDate == null) ? null : String.valueOf(effDate.toEpochMilli()));
		try {
			return (PNGTile) RedisUtils.get(createKey(addr.getName()));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
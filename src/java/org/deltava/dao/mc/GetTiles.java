// Copyright 2012, 2013, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;

import org.deltava.dao.DAOException;

import org.deltava.util.MemcachedUtils;
import org.deltava.util.tile.*;

/**
 * A Data Access Object to read tiles from memcached. 
 * @author Luke
 * @version 6.1
 * @since 5.0
 */

public class GetTiles extends MemcachedDAO {

	/**
	 * Lists the available imagery types.
	 * @return a Collection of types
	 * @throws DAOException if an error occurs
	 */
	public Collection<String> getTypes() throws DAOException {
		setBucket("mapTiles");
		try {
			@SuppressWarnings("unchecked")
			Collection<String> results = (Collection<String>) MemcachedUtils.get(createKey("types"), 100);
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
	public Collection<Date> getDates(String type) throws DAOException {
		setBucket("mapTiles", type);
		try {
			@SuppressWarnings("unchecked")
			Collection<Date> dates = (Collection<Date>) MemcachedUtils.get(createKey("dates"), 100);
			if (dates == null)
				return new HashSet<Date>();
			
			// Validate that the tiles exist
			for (Iterator<Date> i = dates.iterator(); i.hasNext(); ) {
				Date dt = i.next();
				setBucket("mapTiles", type, String.valueOf(dt.getTime()));
				try {
					Object o = MemcachedUtils.get(createKey("$ME"), 100);
					if (o == null) i.remove();
				} catch (Exception e) {
					i.remove();
				}
			}
			
			return dates;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Reads a tile from memcached.
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public PNGTile getTile(String imgType, TileAddress addr) throws DAOException {
		return getTile(imgType, null, addr);
	}
	
	/**
	 * Reads a tile from memcached.
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public PNGTile getTile(String imgType, Date effDate, TileAddress addr) throws DAOException {
		setBucket("mapTiles", imgType, (effDate == null) ? null : Long.valueOf(effDate.getTime()));
		try {
			return (PNGTile) MemcachedUtils.get(createKey(addr.getName()), 150);
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}
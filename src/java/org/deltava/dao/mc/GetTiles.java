// Copyright 2012, 2013, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;

/**
 * A Data Access Object to read tiles from memcached. 
 * @author Luke
 * @version 5.5
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
		Future<Object> f = null;
		try {
			checkConnection();
			f = _client.asyncGet(createKey("types"));
			@SuppressWarnings("unchecked")
			Collection<String> results = (Collection<String>) f.get(100, TimeUnit.MILLISECONDS);
			return (results == null) ? new HashSet<String>() : results;
		} catch (Exception e) {
			cancel(f);
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
		Future<Object> f = null;
		try {
			checkConnection();
			f  =_client.asyncGet(createKey("dates"));
			@SuppressWarnings("unchecked")
			Collection<Date> dates = (Collection<Date>) f.get(100, TimeUnit.MILLISECONDS);
			if (dates == null)
				return new HashSet<Date>();
			
			// Validate that the tiles exist
			for (Iterator<Date> i = dates.iterator(); i.hasNext(); ) {
				Date dt = i.next();
				setBucket("mapTiles", type, String.valueOf(dt.getTime()));
				try {
					f = _client.asyncGet(createKey("$ME"));
					Object o = f.get(100, TimeUnit.MILLISECONDS);
					if (o == null)
						i.remove();
				} catch (Exception e) {
					cancel(f);
					i.remove();
				}
			}
			
			return dates;
		} catch (Exception e) {
			cancel(f);
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
		Future<Object> f = null;
		try {
			checkConnection();
			f = _client.asyncGet(createKey(addr.getName()));
			return (PNGTile) f.get(150, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			cancel(f);
			throw new DAOException(e);
		}
	}
}
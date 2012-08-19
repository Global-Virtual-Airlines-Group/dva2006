// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;

/**
 * A Data Access Object to read tiles from memcached. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class GetTiles extends MemcachedDAO {

	/**
	 * Reads available image dates from memcached. 
	 * @param type the image type
	 * @return a Collection of Dates
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public Collection<Date> getDates(String type) throws DAOException {
		
		setBucket("wuTiles", type);
		Future<Object> f = null;
		try {
			f  =_client.asyncGet(createKey("dates"));
			@SuppressWarnings("unchecked")
			Collection<Date> dates = (Collection<Date>) f.get(125, TimeUnit.MILLISECONDS);
			return (dates == null) ? new HashSet<Date>() : dates;
		} catch (Exception e) {
			f.cancel(true);
			throw new DAOException(e);
		}
	}
	
	/**
	 * Reads a tile from memcached.
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public PNGTile getTile(String imgType, Date effDate, TileAddress addr) throws DAOException {

		setBucket("wuTiles", imgType, String.valueOf(effDate.getTime()));
		Future<Object> f = null;
		try {
			f = _client.asyncGet(createKey(addr));
			PNGTile pt = (PNGTile) f.get(150, TimeUnit.MILLISECONDS);
			return pt;
		} catch (Exception e) {
			f.cancel(true);
			throw new DAOException(e);
		}
	}
}
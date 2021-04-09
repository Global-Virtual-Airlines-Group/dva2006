// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.time.Instant;
import java.util.Collection;

import org.deltava.dao.DAOException;

import org.gvagroup.tile.*;

/**
 * An interface to describe Data Access Objects to read ImageSeries beans.  
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public interface SeriesReader {
	
	/**
	 * Lists the available imagery types.
	 * @return a Collection of types
	 * @throws DAOException if an error occurs
	 */
	public Collection<String> getTypes() throws DAOException;
		
	/**
	 * Reads available image dates for a given type. 
	 * @param type the image type
	 * @return a Collection of Dates
	 * @throws DAOException if a timeout or I/O error occurs
	 */
	public Collection<Instant> getDates(String type) throws DAOException;
	
	/**
	 * Reads a tile.
	 * @param imgType the image type
	 * @param effDate the effective date
	 * @param addr the TileAddress
	 * @return a PNGTile, or null if none
	 * @throws DAOException if an error occurs
	 */
	public PNGTile getTile(String imgType, Instant effDate, TileAddress addr) throws DAOException;
}
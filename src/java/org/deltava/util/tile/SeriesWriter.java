// Copyright 2012, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import org.deltava.dao.DAOException;
import org.gvagroup.tile.ImageSeries;

/**
 * An interface to describe Data Access Objects to write ImageSeries beans. 
 * @author Luke
 * @version 10.0
 * @since 5.0
 */

public interface SeriesWriter {

	/**
	 * Writes an ImageSeries.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occurs
	 */
	public void write(ImageSeries is) throws DAOException;
	
	/**
	 * Purges an ImageSeries.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occurs
	 */
	public void purge(ImageSeries is) throws DAOException;
}
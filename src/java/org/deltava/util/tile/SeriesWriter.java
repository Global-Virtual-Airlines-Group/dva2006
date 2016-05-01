// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import org.deltava.dao.DAOException;

/**
 * An interface to describe Data Access Objects to write ImageSeries beans. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public interface SeriesWriter {

	/**
	 * Writes an ImageSeries.
	 * @param is the ImageSeries
	 * @throws DAOException if an error occurs
	 */
	public void write(org.gvagroup.tile.ImageSeries is) throws DAOException;
}
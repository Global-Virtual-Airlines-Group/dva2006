// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object that loads trans-oceanic track NOTAMs.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public interface TrackDAO {

	/**
	 * Downloads track data.
	 * @return the track data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException;
}
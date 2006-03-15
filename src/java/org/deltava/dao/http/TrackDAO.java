// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object that loads trans-oceanic track NOTAMs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface TrackDAO {

	public String getTrackInfo() throws DAOException;
}
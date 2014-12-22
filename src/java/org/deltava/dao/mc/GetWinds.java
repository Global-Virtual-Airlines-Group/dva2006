// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.mc;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to load winds aloft data from memcached.
 * @author Luke
 * @version 5.4
 * @since 5.4
 */

public class GetWinds extends MemcachedDAO {

	/**
	 * Retrieves the winds for a particular location.
	 * @param loc the GeospaceLocation
	 * @return the closest WindData data
	 * @throws DAOException if a JDBC error occurs
	 */
	public WindData getWinds(GeospaceLocation loc) throws DAOException {
		PressureLevel lvl = PressureLevel.getClosest(loc.getAltitude());
		setBucket("winds", lvl.toString());
		throw new UnsupportedOperationException();
	}
}
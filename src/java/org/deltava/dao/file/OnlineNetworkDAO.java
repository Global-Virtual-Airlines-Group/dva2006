// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import org.deltava.beans.servinfo.NetworkInfo;

import org.deltava.dao.DAOException;

/**
 * An interface for Online Network information Data Access Objects.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public interface OnlineNetworkDAO {

	/**
	 * Retrieves Online Network information.
	 * @return a NetworkInfo bean
	 * @throws DAOException if an I/O error occurs
	 */
	public NetworkInfo getInfo() throws DAOException;
}
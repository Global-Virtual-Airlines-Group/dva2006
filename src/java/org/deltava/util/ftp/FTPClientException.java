// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import org.deltava.dao.DAOException;

/**
 * A class to treat FTP exception as a DAO exception.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FTPClientException extends DAOException {

	/**
	 * Creates a new FTP exception.
	 * @param t the root exception
	 */
	FTPClientException(Throwable t) {
		super(t);
	}
}
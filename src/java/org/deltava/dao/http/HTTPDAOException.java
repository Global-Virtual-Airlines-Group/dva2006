// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.deltava.dao.DAOException;

/**
 * An Exception thrown by HTTP Data Access Objects.
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public class HTTPDAOException extends DAOException {
	
	private int _statusCode;

	/**
	 * Creates a new DAO Exception.
	 * @param t the inner Exception
	 */
	HTTPDAOException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new DAO Exception.
	 * @param msg the message
	 * @param code the HTTP status code
	 */
	HTTPDAOException(String msg, int code) {
		super(msg + " - " + code);
		_statusCode = code;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code, or zero if not set
	 */
	@Override
	public int getStatusCode() {
		return _statusCode;
	}
}
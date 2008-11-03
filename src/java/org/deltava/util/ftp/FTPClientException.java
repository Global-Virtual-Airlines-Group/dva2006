// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import com.enterprisedt.net.ftp.FTPException;

import org.deltava.dao.DAOException;

/**
 * A class to treat FTP exceptions as a DAO exception.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class FTPClientException extends DAOException {
	
	private int _resultCode;

	/**
	 * Creates a new FTP exception from another exception.
	 * @param t the root exception
	 */
	FTPClientException(Throwable t) {
		super(t);
		if (t instanceof FTPException)
			_resultCode = ((FTPException) t).getReplyCode();
	}
	
	/**
	 * Creates a new FTP exception with a message.
	 * @param msg the exception message
	 */
	FTPClientException(String msg) {
		super(msg);
	}
	
	/**
	 * Returns the FTP error code.
	 * @return the error code
	 */
	public int getResultCode() {
		return _resultCode;
	}
	
	/**
	 * Sets the FTP error code.
	 * @param code the error code
	 */
	public void setResultCode(int code) {
		_resultCode = code;
	}
}
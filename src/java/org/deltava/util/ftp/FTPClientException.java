// Copyright 2006, 2008, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import com.enterprisedt.net.ftp.FTPException;

import org.deltava.dao.DAOException;

/**
 * A class to treat FTP exceptions as a DAO exception.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class FTPClientException extends DAOException {
	
	private final boolean _dumpStack;
	private final int _resultCode;

	/**
	 * Creates a new FTP exception from another exception.
	 * @param dumpStack TRUE to dump the stack trace, otherwise FALSE
	 * @param t the root exception
	 */
	FTPClientException(Throwable t, boolean dumpStack) {
		super(t);
		_dumpStack = dumpStack;
		_resultCode = (t instanceof FTPException fe) ? fe.getReplyCode() : 0;
	}
	
	/**
	 * Creates a new FTP exception with a message.
	 * @param msg the exception message
	 */
	FTPClientException(String msg) {
		super(msg);
		_dumpStack = true;
		_resultCode = 0;
	}
	
	/**
	 * Returns the FTP error code.
	 * @return the error code
	 */
	public int getResultCode() {
		return _resultCode;
	}
	
	/**
	 * Returns whether the exception handler should display the stack trace.
	 * @return TRUE to display the stack trace, otherwise FALSE
	 */
	public boolean getDumpStack() {
		return _dumpStack;
	}
}
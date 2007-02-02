// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

/**
 * An Exception thrown by Web Services.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServiceException extends Exception {

	private int _httpCode;
	private boolean _logStackDump;

	/**
	 * Creates a new Web Service Exception from an existing exception.
	 * @param code the HTTP result code
	 * @param msg the exception message
	 * @param t the existing exception
	 */
	ServiceException(int code, String msg, Throwable t) {
		super(msg, t);
		_httpCode = code;
	}

	/**
	 * Creates a new Web Service Exception.
	 * @param code the HTTP result code
	 * @param msg the exception message
	 */
	ServiceException(int code, String msg) {
		this(code, msg, true);
	}

	/**
	 * Creates a new Web Service Exception.
	 * @param code the HTTP result code
	 * @param msg the exception message
	 * @param dumpStack TRUE if the stack should be dumped, otherwise FALSE
	 */
	ServiceException(int code, String msg, boolean dumpStack) {
		super(msg);
		_httpCode = code;
		_logStackDump = dumpStack;
	}

	/**
	 * Returns wether the handling class should log this Exception's stack trace.
	 * @return TRUE if the stack trace should be logged, otherwise FALSE
	 * @see ServiceException#setLogStackDump(boolean)
	 */
	public boolean getLogStackDump() {
		return _logStackDump;
	}

	/**
	 * Updates wether the handling class should log this Exception's stack trace.
	 * @param doLog TRUE if the stack trace should be logged, otherwise FALSE
	 * @see ServiceException#getLogStackDump()
	 */
	public void setLogStackDump(boolean doLog) {
		_logStackDump = doLog;
	}

	/**
	 * Returns the HTTP result code.
	 * @return the HTTP result code
	 */
	public int getCode() {
		return _httpCode;
	}
}
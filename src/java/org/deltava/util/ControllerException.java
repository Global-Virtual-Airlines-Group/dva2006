// Copyright 2006, 2012, 2015, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * An exception class to define a common singure for exceptions handled by the Command
 * Controller servlet.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public abstract class ControllerException extends Exception {
	
	private enum ErrorLogLevel {
		ERROR, WARNING, NONE
	}

	private boolean _logStackDump = true;
	private ErrorLogLevel _level = ErrorLogLevel.ERROR;
	private int _statusCode = SC_INTERNAL_SERVER_ERROR;
	private String _fwdURL;
	
    /**
     * Wraps a thrown exception. This assumes the stack trace of the original execption. 
     * @param t the thrown exception to wrap
     * @see Throwable#getCause()
     */
    public ControllerException(Throwable t) {
        super(t.getMessage(), t);
    }

    /**
     * Creates a new controller exception with a given message.
     * @param msg the message for the exception
     */
    public ControllerException(String msg) {
        super(msg);
    }
    
    /**
     * Create a new ControllerException with an error message and underlying cause.
     * @param msg the error message
     * @param t the root cause Exception
     */
    public ControllerException(String msg, Throwable t) {
        super(msg, t);
    }
	
	/**
	 * Returns whether the handling class should log this Exception's stack trace.
	 * @return TRUE if the stack trace should be logged, otherwise FALSE
	 * @see ControllerException#setLogStackDump(boolean)
	 */
	public boolean getLogStackDump() {
		return _logStackDump;
	}
	
	/**
	 * Returns the URL that the Controller servlet should forward the request to.
	 * @return the URL to forward to, or null
	 * @see ControllerException#setForwardURL(String)
	 */
	public String getForwardURL() {
		return _fwdURL;
	}
	
	/**
	 * Returns whether this exception to be logged as a warning, not an error.
	 * @return TRUE if a warning, otherwise FALSE
	 * @see ControllerException#setWarning(boolean)
	 */
	public boolean isWarning() {
		return (_level == ErrorLogLevel.WARNING);
	}
	
	/**
	 * Returns whether this exception should not be logged.
	 * @return TRUE if not logged, otherwise FALSE
	 * @see ControllerException#setSuppressed(boolean)
	 */
	public boolean isSuppressed() {
		return (_level == ErrorLogLevel.NONE);
	}
	
	/**
	 * Returns the HTTP status code to set on the error page.
	 * @return the HTTP status code
	 * @see ControllerException#setStatusCode(int)
	 */
	public int getStatusCode() {
		return _statusCode;
	}
	
	/**
	 * Updates whether the handling class should log this Exception's stack trace.
	 * @param doLog TRUE if the stack trace should be logged, otherwise FALSE
	 * @see ControllerException#getLogStackDump()
	 */
	public void setLogStackDump(boolean doLog) {
		_logStackDump = doLog;
	}
	
	/**
	 * Updates the URL that the Controller servlet should forward the request to.
	 * @param url the URL to forward to
	 * @see ControllerException#getForwardURL()
	 */
	public void setForwardURL(String url) {
		_fwdURL = url;
	}
	
	/**
	 * Sets this exception to be logged as a warning, not an error.
	 * @param isWarn TRUE if a warning, otherwise FALSE
	 * @see ControllerException#isWarning()
	 */
	public void setWarning(boolean isWarn) {
		_level = isWarn ? ErrorLogLevel.WARNING : ErrorLogLevel.ERROR;
	}
	
	/**
	 * Sets this exception to not be logged.
	 * @param isSuppress TRUE if suppressed, otherwise FALSE
	 * @see ControllerException#isSuppressed()
	 */
	public void setSuppressed(boolean isSuppress) {
		_level = isSuppress ? ErrorLogLevel.NONE : ErrorLogLevel.ERROR;
	}
	
	/**
	 * Sets the HTTP status code to set on the error page.
	 * @param code the HTTP status code
	 * @see ControllerException#getStatusCode()
	 */
	public void setStatusCode(int code) {
		_statusCode = code;
	}
}
// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

/**
 * An exception class to define a common singure for exceptions handled by the Command
 * Controller servlet.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ControllerException extends Exception {

	private boolean _logStackDump;
	private boolean _isWarn;
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
	 * Returns wether the handling class should log this Exception's stack trace.
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
	 * Returns wether this exception to be logged as a warning, not an error.
	 * @return TRUE if a warning, otherwise FALSE
	 * @see ControllerException#setWarning(boolean)
	 */
	public boolean isWarning() {
		return _isWarn;
	}
	
	/**
	 * Updates wether the handling class should log this Exception's stack trace.
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
		_isWarn = isWarn;
	}
}
// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

/**
 * An exception to flag invalid data while preserving code flow.  
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class InvalidDataException extends Exception {

	/**
	 * Creates a new Exception.
	 * @param msg the exception message
	 */
	InvalidDataException(String msg) {
		super(msg);
	}

	/**
	 * Returns an XACARS-formatted message.
	 * @return the message prepended with a status code
	 */
	public String getResponse() {
		StringBuilder buf = new StringBuilder("0|");
		buf.append(getMessage());
		return buf.toString();
	}
}
// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An exception thrown when validating an ACARS position archive. This is an unchecked exception to allow selective handling.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class ArchiveValidationException extends RuntimeException {

	/**
	 * Creates the exception.
	 * @param msg the exception message
	 */
	public ArchiveValidationException(String msg) {
		super(msg);
	}

	/**
	 * Creates the exception from a parent Exception.
	 * @param e the Exception
	 */
	public ArchiveValidationException(Exception e) {
		super(e);
	}
}
// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * An exception to store test/promotion ineligibility reasons.
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class IneligibilityException extends Exception {

	/**
	 * Creates the exception.
	 * @param msg the ineligibility reason
	 */
	IneligibilityException(String msg) {
		super(msg);
	}
}
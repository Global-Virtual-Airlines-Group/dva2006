// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.commands.CommandException;

/**
 * An exception thrown by an access controller. By default, stack traces of access
 * control exceptions are not logged.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AccessControlException extends CommandException {
	
	/**
	 * Cretes a new access control exception with a particular message.
	 * @param msg the message
	 */
	AccessControlException(String msg) {
		super(msg);
		setLogStackDump(false);
	}
	
	/**
	 * Creates a new access control exception with a particular message, and optionally
	 * sets the warning flag.
	 * @param msg the message
	 * @param doWarn TRUE if the exception should be logged as a warning, otherwise FALSE
	 */
	AccessControlException(String msg, boolean doWarn) {
		this(msg);
		setWarning(doWarn);
	}
}
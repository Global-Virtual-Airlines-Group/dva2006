// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

/**
 * An exception thrown on a security contraint violation during command execution.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandSecurityException extends CommandException {

    /**
     * Create a new security exception.
     * @param msg the exception message
     */
    public CommandSecurityException(String msg) {
        super(msg);
    }
}
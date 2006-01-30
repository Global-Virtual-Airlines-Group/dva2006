// Copyright (c) 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import org.deltava.servlet.ControllerException;

/**
 * An exception thrown by a Data Access Object.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DAOException extends ControllerException {
	
    /**
     * Wraps a thrown exception inside a DAO exception. This assumes the stack trace of the original execption. 
     * @param t the thrown exception to wrap
     * @see Throwable#getCause()
     */
    public DAOException(Throwable t) {
        super(t);
    }

    /**
     * Creates a new DAO exception with a given message.
     * @param msg the message for the exception
     */
    public DAOException(String msg) {
        super(msg);
    }
}
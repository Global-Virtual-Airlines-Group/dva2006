// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import org.deltava.servlet.ControllerException;

/**
 * An exception thrown by a Web Site Command.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see Command
 */

public class CommandException extends ControllerException {
	
    /**
     * Create a new CommandException with an error message.
     * @param msg the error message
     */
    public CommandException(String msg) {
        super(msg);
    }
    
    /**
     * Creates a new CommandException with an error message, and controls stack dumping.
     * @param msg
     * @param dumpStack
     */
    public CommandException(String msg, boolean dumpStack) {
    	this(msg);
    	setLogStackDump(dumpStack);
    }

    /**
     * Create a new CommandException with an error message and underlying cause.
     * @param msg the error message
     * @param t the root cause Exception
     * @see Throwable#getCause()
     */
    public CommandException(String msg, Throwable t) {
        super(msg + " - " + t.getClass().getName(), t);
        setStackTrace(t.getStackTrace());
    }
    
    /**
     * Create a new CommandException that wraps an underlying exception.
     * @param t the root cause Exception.
     */
    public CommandException(Throwable t) {
        this(t.getMessage(), t);
    }
    
    /**
     * Create a new CommandException that wraps another Controller exception. The intermediate
     * exception will be stripped out.
     * @param ce the root Controller Exception
     */
    public CommandException(ControllerException ce) {
       this((ce.getCause() == null) ? ce : ce.getCause());
       setLogStackDump(ce.getLogStackDump());
    }
}
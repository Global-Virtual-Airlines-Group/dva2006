package org.deltava.commands;

import org.deltava.dao.DAOException;

/**
 * An exception thrown by a web site command.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see Command
 */

public class CommandException extends Exception {

    /**
     * Create a new CommandException with an error message.
     * @param msg the error message
     */
    public CommandException(String msg) {
        super(msg);
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
     * Create a new CommandException that wraps a DAO exception. The intermediate DAO exception
     * will be stripped out.
     * @param de the root DAO Exception
     */
    public CommandException(DAOException de) {
       this((de.getCause() == null) ? de : de.getCause());
    }
}
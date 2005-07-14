package org.deltava.dao;

/**
 * An exception thrown by a Data Access Object.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class DAOException extends Exception {

    /**
     * Wraps a thrown exception inside a DAO exception. This assumes the stack trace of the original execption. 
     * @param t the thrown exception to wrap
     * @see Throwable#getCause()
     */
    public DAOException(Throwable t) {
        super(t.getMessage());
        initCause(t);
        setStackTrace(t.getStackTrace());
    }

    /**
     * Creates a new DAO exception with a given message.
     * @param msg the message for the exception
     */
    public DAOException(String msg) {
        super(msg);
    }
}
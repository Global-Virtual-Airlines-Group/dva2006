package org.deltava.jdbc;

import org.deltava.dao.DAOException;

/**
 * An exception thrown when a Connection Pool error occurs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionPoolException extends DAOException {

    /**
     * Creates a new exception with a given message.
     * @param msg the error message
     */
    public ConnectionPoolException(String msg) {
        super(msg);
    }
    
    /**
     * Creates a new exception from an existing exception.
     * @param t the nested exception
     */
    public ConnectionPoolException(Throwable t) {
        super(t);
    }
}
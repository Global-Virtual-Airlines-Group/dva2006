package org.deltava.jdbc;

import org.deltava.dao.DAOException;

/**
 * An exception thrown when a Transaction error occurs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransactionException extends DAOException {

    /**
     * Creates a new exception from an existing exception.
     * @param t the nested exception
     */
    public TransactionException(Throwable t) {
        super(t);
    }
}
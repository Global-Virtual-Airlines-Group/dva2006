// Copyright 2004 Global Virtual Airlines Grouup. All Rights Reserved.
package org.deltava.crypt;

/**
 * An internal Cryptographic exception.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CryptoException extends RuntimeException {

    /**
     * Creates a new CryptoException.
     * @param msg
     */
    CryptoException(String msg) {
        super(msg);
    }
    
    /**
     * Creates a new CryptoException with a nested exception.
     * @param msg the exception message
     * @param t the root cause of the message
     * @see Throwable#getCause()
     */
    CryptoException(String msg, Throwable t) {
        super(msg + " - " + t.getClass().getName(), t);
        setStackTrace(t.getStackTrace());
    }
}
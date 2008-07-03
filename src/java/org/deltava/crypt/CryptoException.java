// Copyright 2004, 2008 Global Virtual Airlines Grouup. All Rights Reserved.
package org.deltava.crypt;

/**
 * An internal Cryptographic exception.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class CryptoException extends RuntimeException {
	
	private Object _payload;

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
    
    /**
     * Creates a new CryptoException with a nested exception and payload.
     * @param msg the exception message
     * @param t the root cause of the message
     * @param payload the payload
     * @see Throwable#getCause()
     */
    CryptoException(String msg, Throwable t, Object payload) {
        this(msg + " - " + t.getClass().getName(), t);
        _payload = payload;
    }
    
    /**
     * Returns the invalid cryptogrphic payload.
     * @return the payload
     */
    public Object getPayload() {
    	return _payload;
    }
}
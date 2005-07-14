package org.deltava.dao.http;

import java.io.Serializable;

import java.io.*;
import java.net.*;

/**
 * An abstract class to support HTTP-based Data Access Objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DAO implements Serializable {

    private static final int BUFFER_SIZE = 10240;
    
    protected URLConnection _c;
    private int _size = BUFFER_SIZE;
    
    /**
     * Initializes the DAO with a particular URL connection. We use the generic superclass so that files can be loaded.
     * @param c the URL connection 
     */
    public DAO(URLConnection c) {
        super();
        _c = c;
    }
    
    /**
     * Sets the buffer size for the connection.
     * @param size the size in bytes
     * @throws IllegalArgumentException if size is zero or negative
     */
    public void setBufferSize(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Invalid buffer size - " + size);
        
        _size = size;
    }

    /**
     * Returns a reader suitable for text reads on the HTTP connection.
     * @return a BufferedReader
     * @throws IOException if a network error occurs
     */
    protected BufferedReader getReader() throws IOException {
        InputStream in = _c.getInputStream();
        return new BufferedReader(new InputStreamReader(in), _size);
    }
}
// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

/**
 * An abstract class to support stream-based Data Access Objects.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public abstract class DAO {

    private InputStream _is;
    private int _size = 10240;
    
    /**
     * Initializes the Data Access Object with a particular input stream.
     * @param is the input stream
     */
    protected DAO(InputStream is) {
    	super();
    	_is = is;
    }
    
    /**
     * Sets the buffer size for the connection.
     * @param size the size in bytes
     */
    public void setBufferSize(int size) {
        _size = Math.max(0, size);
    }

    /**
     * Returns a reader suitable for text reads on the HTTP connection.
     * @return a BufferedReader
     */
    protected LineNumberReader getReader() {
        return new LineNumberReader(new InputStreamReader(_is), _size);
    }
    
    /**
     * Returns the input stream used for input.
     * @return an InputStream
     */
    protected InputStream getStream() {
    	return _is;
    }
}
// Copyright 2004, 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;

/**
 * An abstract class to implement support for image/file buffers in a bean.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class DatabaseBlobBean extends DatabaseBean {

    /**
     * The buffer for the image/file data.
     */
    protected byte[] _buffer;
    
    /**
     * Returns wether the buffer has been populated.
     * @return TRUE if the buffer has any data, otherwise FALSE
     */
    public boolean isLoaded() {
    	return (_buffer != null);
    }
    
    /**
     * Returns the content of the buffer. There is no requirement that beans implementing a buffer
     * populate the buffer except when necessary.
     * @return an InputStream with the buffer's contents, or an empty stream if the buffer is null
     * @see DatabaseBlobBean#isLoaded()
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(isLoaded() ? _buffer : new byte[0]);
    }

    /**
     * Returns the size of the image/file data.
     * @return the size of the data, in bytes. If the buffer is not initialized, returns -1
     * @see DatabaseBlobBean#isLoaded()
     */
    public int getSize() {
        return isLoaded() ? _buffer.length : -1;
    }
    
    /**
     * Loads the contents of a stream into the buffer. The data should be available at the specified input stream, and
     * the stream is <u>not</u> closed when this method completes.
     * @param is the stream containing the data
     * @throws IOException if an error occurs reading the stream data
     * @see DatabaseBlobBean#load(byte[])    
     */
    public void load(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
        byte[] buf = new byte[16384];
        int bytesRead = is.read(buf);
        while (bytesRead != -1) {
            os.write(buf, 0, bytesRead);
            bytesRead = is.read(buf);
        }
        
        os.close();
        _buffer = os.toByteArray();
    }
    
    /**
     * Replaces the buffer data.
     * @param buffer the new buffer data
     * @see DatabaseBlobBean#load(InputStream)
     */
    public void load(byte[] buffer) {
        _buffer = buffer;
    }
    
    /**
     * Clears the buffer.
     */
    public void clear() {
    	_buffer = null;
    }
}
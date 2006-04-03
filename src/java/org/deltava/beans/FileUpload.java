// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;

/**
 * A bean to store File Upload data in an HTTP servlet request.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileUpload {

    private String _name;
    private byte[] _buffer;
    
    /**
     * Creates a new bean with a given name.
     * @param name the name of the file
     * @see FileUpload#getName()
     */
    public FileUpload(String name) {
        super();
        _name = name;
    }

    /**
     * Returns the name of the file.
     * @return the file name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the size of the buffer.
     * @return the buffer size, or zero if not loaded
     */
    public int getSize() {
        return (_buffer == null) ? 0 : _buffer.length;
    }
    
    /**
     * Returns the buffer.
     * @return the buffer
     * @see FileUpload#getInputStream()
     */
    public byte[] getBuffer() {
        return _buffer;
    }
    
    /**
     * Returns an input stream backed by the buffer.
     * @return an input stream
     * @see FileUpload#getBuffer()
     */
    public InputStream getInputStream() {
    	return new ByteArrayInputStream(_buffer);
    }
    
    /**
     * Loads the buffer from an input stream.
     * @param is the stream containing the data
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream is) throws IOException {
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(16384);
        byte[] buffer = new byte[16384];
        int bytesRead = is.read(buffer);
        while (bytesRead != -1) {
            outStream.write(buffer, 0, bytesRead);
            bytesRead = is.read(buffer);
        }
        
        outStream.close();
        _buffer = outStream.toByteArray();
    }
}
// Copyright 2005, 2006, 2007, 2013, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;
import java.util.zip.*;

import org.deltava.util.BZip2MultiInputStream;

/**
 * A bean to store File Upload data in an HTTP servlet request. This bean can automatically detect compressed (GZIP/BZIP2) content and creates the appropriate 
 * decompresser output stream. This allows reduced memory usage when uploading large files.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class FileUpload {

    private final String _name;
    private byte[] _buffer;
    
    /**
     * Creates a new bean with a given name.
     * @param name the name of the file
     * @throws NullPointerException if name is null
     */
    public FileUpload(String name) {
        super();
        _name = name.toLowerCase();
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
     * Returns an input stream backed by the buffer. This will automatically generate a
     * {@link GZIPInputStream} if the data is already in GZIP format. If the raw data needs to be
     * obtained, wrapping a {@link ByteArrayInputStream} around the buffer should be used.
     * @return an input stream
     * @see FileUpload#getBuffer()
     */
    public InputStream getInputStream() {
    	InputStream is = new ByteArrayInputStream(_buffer);
    	try {
    		if (_name.endsWith(".gz"))
    			return new GZIPInputStream(is);
    		else if (_name.endsWith(".bz2"))
    			return new BZip2MultiInputStream(is);
    	} catch (Exception e) {
    		return is;
    	}
    	
    	return is;
    }
    
    /**
     * Loads the buffer from an input stream.
     * @param is the stream containing the data
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream is) throws IOException {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream(32768)) {
        	byte[] buffer = new byte[40960];
        	int bytesRead = is.read(buffer);
        	while (bytesRead != -1) {
        		outStream.write(buffer, 0, bytesRead);
        		bytesRead = is.read(buffer);
        	}
        
        	_buffer = outStream.toByteArray();
        }
    }
}
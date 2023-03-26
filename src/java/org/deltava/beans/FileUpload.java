// Copyright 2005, 2006, 2007, 2013, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;

/**
 * A bean to store File Upload data in an HTTP servlet request. This bean can automatically detect compressed (GZIP/BZIP2) content and creates the appropriate 
 * decompresser output stream. This allows reduced memory usage when uploading large files.
 * @author Luke
 * @version 10.5
 * @since 1.0
 */

public class FileUpload {

    private final String _name;
    private final Compression _compress;
    private byte[] _buffer;
    
    /**
     * Creates a new bean with a given name.
     * @param name the name of the file
     * @throws NullPointerException if name is null
     */
    public FileUpload(String name) {
        super();
        _name = name.toLowerCase();
        _compress = Compression.get(name);
    }

    /**
     * Returns the name of the file.
     * @return the file name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the compression type.
     * @return the Compression
     */
    public Compression getCompression() {
    	return _compress;
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
     * Returns an input stream backed by the buffer. This will automatically generate an appropriate compression inputstream if the data is in a compressed format.
     * @return an input stream
     * @see FileUpload#getBuffer()
     * @see Compression#getStream(InputStream)
     */
    public InputStream getInputStream() {
    	InputStream is = new ByteArrayInputStream(_buffer);
    	try {
    		return _compress.getStream(is);
    	} catch (Exception e) {
    		return is;
    	}
    }
    
    /**
     * Loads the buffer from an input stream.
     * @param is the stream containing the data
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream is) throws IOException {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream(16384)) {
        	int b = is.read();
        	while (b != -1) {
        		outStream.write(b);
        		b = is.read();
        	}
        
        	_buffer = outStream.toByteArray();
        }
    }
}
package org.deltava.beans;

import java.io.*;

/**
 * An abstract class to implement support for image/file buffers in a bean.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public abstract class DatabaseBlobBean extends DatabaseBean {

    /**
     * The buffer for the image/file data.
     */
    protected byte[] _buffer;
    
    /**
     * Returns the content of the buffer. There is no requirement that beans implementing a buffer
     * populate the buffer except when necessary.
     * @return an InputStream with the buffer's contents, or an empty stream if the buffer is null
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream((_buffer == null) ? new byte[0] : _buffer);
    }

    /**
     * Returns the size of the image/file data.
     * @return the size of the data, in bytes. If the buffer is not initialized, returns -1
     */
    public int getSize() {
        return (_buffer == null) ? -1 : _buffer.length;
    }
    
    /**
     * Loads the contents of a stream into the buffer. The data should be available at the specified input stream, and
     * the stream is <u>not</u> closed when this method completes.
     * @param is the stream containing the data
     * @throws IOException if an error occurs reading the stream data    
     */
    public void load(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
        byte[] buf = new byte[16384];
        int bytesRead = is.read(buf);
        while (bytesRead != -1) {
            os.write(buf, 0, bytesRead);
            bytesRead = is.read(buf);
        }
        
        os.close();
        _buffer = os.toByteArray();
    }
    
    public void load(byte[] buffer) {
        _buffer = buffer;
    }
}
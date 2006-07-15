// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * A class to generate MD5/SHA message digests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageDigester {

    private int _bufferSize = 1024;
    private MessageDigest _md;

    /**
     * Create a new Message Digester with a specified algorithm.
     * @param algorithm the algorithm to use; must be supported by the JVM 
     * @throws CryptoException
     */
    public MessageDigester(String algorithm) throws CryptoException {
        super();
        try {
            _md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            throw new CryptoException("Cannot create " + algorithm + " MessageDigest", e);
        }
    }

    /**
     * Create a new Message Digester with a specified algorithm and input buffer size.
     * @param algorithm the algorithm to use; must be supported by the JVM
     * @param bufSize the buffer Size in bytes.
     * @see MessageDigester#setBufferSize(int)
     */
    public MessageDigester(String algorithm, int bufSize) {
        this(algorithm);
        setBufferSize(bufSize);
    }
    
    /**
     * &quot;Salts&quot; the message digest by processing a string before the actual data. This is
     * used to protect (somewhat) against reverse engineering the hash through brute force.
     * @param saltValue the salt string
     * @throws NullPointerException if saltValue is null 
     */
    public void salt(String saltValue) {
       _md.update(saltValue.getBytes());
    }

    /**
     * Return the Message Digest algorithm in use.
     * @return the algorithm name
     */
    public final String getAlgorithm() {
        return _md.getAlgorithm();
    }

    /**
     * Updates the buffer size.
     * @param size the new buffer size in bytes. Must be > 64
     */
    public final void setBufferSize(int size) {
        if (size > 64)
            _bufferSize = size;
    }

    /**
     * Returns the message digest value for a given amount of data.
     * @param data the data to digest
     * @return the message digest
     */
    public byte[] digest(byte[] data) {
        synchronized (_md) {
            return _md.digest(data);
        }
    }

    /**
     * Returns the message digest value for the data within a given stream.
     * @param is the stream containing the data
     * @return the message digest
     * @throws IOException if an error occurs reading the data
     */
    public byte[] digest(InputStream is) throws IOException {
        byte[] buffer = new byte[_bufferSize];
        synchronized (_md) {
            int bytesRead = is.read(buffer);
            while (bytesRead > 0) {
                _md.update(buffer, 0, bytesRead);
                bytesRead = is.read(buffer);
            }

            return _md.digest();
        }
    }
    
    /**
     * Resets the Message Digester.
     */
    public void reset() {
       _md.reset();
    }
    
    /**
     * Converts a message digest into a hexadecimal String.
     * @param hash the message digest
     * @return the hexadecimal representation of the message digest
     */
    public static String convert(byte[] hash) {
    	if (hash == null)
    		return null;
    	
    	StringBuilder buf = new StringBuilder(hash.length << 1);
    	for (int x = 0; x < hash.length; x++) {
    		int b = hash[x];
    		if (b < 0)
    			b += 256;
    		
    		if (b < 0x10)
    			buf.append('0');
    			
    		buf.append(Integer.toHexString(b).toLowerCase());
    	}
    	
    	return buf.toString();
    }
    
    /**
     * Converts a hexadecimal String into a byte array.
     * @param hash the hexadecimal message digest
     * @return the message digest
     */
    public static byte[] parse(String hash) { 
    	if ((hash == null) || ((hash.length() & 0x1) == 1))
    		throw new IllegalArgumentException("Invalid Hash - " + hash);
    	
    	byte[] results = new byte[hash.length() >> 1];
    	for (int x = 0; x < hash.length(); x += 2) {
    		results[x >> 1] = (byte) Integer.parseInt(hash.substring(x, x + 2), 16);
    	}
    	
    	return results;
    }
}
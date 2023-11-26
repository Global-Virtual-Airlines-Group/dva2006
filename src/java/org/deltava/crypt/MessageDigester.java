// Copyright 2004, 2005, 2006, 2011, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import java.io.*;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * A class to generate MD5/SHA message digests.
 * @author Luke
 * @version 11.1
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
    	_bufferSize = Math.max(64, size);
    }

    /**
     * Returns the message digest value for a given amount of data.
     * @param data the data to digest
     * @return the message digest
     */
    public byte[] digest(byte[] data) {
    	return _md.digest(data);
    }

    /**
     * Returns the message digest value for the data within a given stream.
     * @param is the stream containing the data
     * @return the message digest
     * @throws IOException if an error occurs reading the data
     */
    public byte[] digest(InputStream is) throws IOException {
        byte[] buffer = new byte[_bufferSize];
        int bytesRead = is.read(buffer);
        while (bytesRead > 0) {
        	_md.update(buffer, 0, bytesRead);
            bytesRead = is.read(buffer);
        }

        return _md.digest();
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
    	return (hash == null) ? null : HexFormat.of().formatHex(hash);
    }
    
    /**
     * Converts a hexadecimal String into a byte array.
     * @param hash the hexadecimal message digest
     * @return the message digest
     */
    public static byte[] parse(String hash) { 
    	if ((hash == null) || ((hash.length() & 0x1) == 1))
    		throw new IllegalArgumentException("Invalid Hash - " + hash);
    	
    	return HexFormat.of().parseHex(hash);
    }
}
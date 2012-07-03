// Copyright 2005, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import javax.crypto.spec.SecretKeySpec;

/**
 * A class to encrypt/decrypt data using Blowfish.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public final class BlowfishEncryptor extends SecretKeyEncryptor {

    // This helper method ensure the key is 8 bytes long
    private static byte[] trimKeySize(byte[] rawKey) {
        byte[] result = new byte[8];
        System.arraycopy(rawKey, 0, result, 0, 8);
        return result;
    }
    
    /**
     * Create a new Blowifhs encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 8 bytes are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public BlowfishEncryptor(byte[] keyData) {
        super("Blowfish");
        try {
            setKey(new SecretKeySpec(trimKeySize(keyData), "Blowfish"));
        } catch (Exception e) {
            throw new CryptoException("Cannot generate Blowfish Encryptor", e);
        }
    }
    
    
    /**
     * Create a new Blowfish encryptor given an arbitrary key string.
     * @param keyData the key string. Only the first 8 characters are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public BlowfishEncryptor(String keyData) {
        this(keyData.getBytes());
    }
}
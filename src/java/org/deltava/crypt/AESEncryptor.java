// Copyright 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import javax.crypto.spec.SecretKeySpec;

/**
 * A class to encrypt/decrypt data using AES.
 * @author Luke
 * @version 10.1
 * @since 6.4
 */

public final class AESEncryptor extends SecretKeyEncryptor {

    /**
     * Create a new AES encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 16 or 32 bytes are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public AESEncryptor(byte[] keyData) throws CryptoException {
        super("AES");
        try {
            setKey(new SecretKeySpec(trimKeySize(keyData, (keyData.length > 32) ? 32 : 16), "AES"), null);
        } catch (Exception e) {
            throw new CryptoException("Cannot generate AES Encryptor", e);
        }
    }
    
    /**
     * Create a new AES encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 16 or 32 bytes are used
     * @param iv the Initialization Vector, or null
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public AESEncryptor(byte[] keyData, byte[] iv) throws CryptoException {
        super("AES/CBC/PKCS5Padding");
        try {
            setKey(new SecretKeySpec(trimKeySize(keyData, (keyData.length > 32) ? 32 : 16), "AES"), iv);
        } catch (Exception e) {
            throw new CryptoException("Cannot generate AES Encryptor", e);
        }
    }
    
    /**
     * Create a new AES encryptor given an arbitrary key string.
     * @param keyData the key string. Only the first 24 characters are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public AESEncryptor(String keyData) {
        this(keyData.getBytes());
    }
}
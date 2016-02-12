package org.deltava.crypt;

import javax.crypto.spec.SecretKeySpec;

/**
 * A class to encrypt/decrypt data using AES.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class AESEncryptor extends SecretKeyEncryptor {

    /**
     * Create a new AES encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 16 bytes are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public AESEncryptor(byte[] keyData) throws CryptoException {
        super("AES");
        try {
            setKey(new SecretKeySpec(trimKeySize(keyData, 16), "AES"));
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
package org.deltava.crypt;

import javax.crypto.spec.DESedeKeySpec;

/**
 * A class to encrypt/decrypt data using 3DES
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class DESEncryptor extends SecretKeyEncryptor {

    /**
     * Create a new 3DES encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 24 bytes are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public DESEncryptor(byte[] keyData) throws CryptoException {
        super("DESede");
        try {
            initKey(new DESedeKeySpec(keyData));
        } catch (Exception e) {
            throw new CryptoException("Cannot generate 3DES Encryptor", e);
        }
    }
    
    /**
     * Create a new 3DES encryptor given an arbitrary key string.
     * @param keyData the key string. Only the first 24 characters are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public DESEncryptor(String keyData) {
        this(keyData.getBytes());
    }
}
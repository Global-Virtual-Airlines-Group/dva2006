// Copyright 2005, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import javax.crypto.spec.SecretKeySpec;

/**
 * A class to encrypt/decrypt data using Blowfish.
 * @author Luke
 * @version 6.4
 * @since 1.0
 */

public final class BlowfishEncryptor extends SecretKeyEncryptor {

    /**
     * Create a new Blowfish encryptor given an arbitrary key.
     * @param keyData the key data. Only the first 8 bytes are used
     * @throws CryptoException if the encryptor cannot be initialized
     */
    public BlowfishEncryptor(byte[] keyData) {
        super("Blowfish");
        try {
            setKey(new SecretKeySpec(trimKeySize(keyData, 8), "Blowfish"));
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
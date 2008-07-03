// Copyright 2004, 2008 Global Virtual Airlines Grouup. All Rights Reserved.
package org.deltava.crypt;

import java.security.spec.KeySpec;

import javax.crypto.*;

/**
 * A class to support encrypting/decrypting data using a symetric secret key. 
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public abstract class SecretKeyEncryptor {

    private String _algorithm;
    
    private Cipher _cipher;
    private SecretKey _key;
    
    /**
     * Create a new encryptor using a given algorithm.
     * @param algorithm the encryption algorithm to use.
     */
    protected SecretKeyEncryptor(String algorithm) {
        super();
        _algorithm = algorithm;
    }
    
    /**
     * Initialize the encryptor using a given key specification.
     * @param spec the secret Key information
     * @throws CryptoException if the Secret Key cannot be initialized by the JVM
     */
    protected void initKey(KeySpec spec) {
        try {
            _cipher = Cipher.getInstance(_algorithm);            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(_algorithm);
            _key = factory.generateSecret(spec);
        } catch (Exception e) {
            throw new CryptoException("Cannot create " + _algorithm + " Cipher/Key", e);
        }
    }
    
    /**
     * Initialize the encryptor using a given secret key.
     * @param key the secret Key
     * @throws CryptoException if the cipher cannot be initialized by the JVM
     */
    protected void setKey(SecretKey key) {
        try {
            _cipher = Cipher.getInstance(_algorithm);
            _key = key;
        } catch (Exception e) {
            throw new CryptoException("Cannot create " + _algorithm + " Cipher", e);
        }
    }
    
    /**
     * Returns the algorithm in use.
     * @return the algorithm name
     */
    public final String getAlgorithm() {
        return _algorithm;
    }
    
    /**
     * Encrypt a given amount of data.
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws CryptoException if something bad happens
     */
    public synchronized byte[] encrypt(byte[] data) throws CryptoException {
        try {
            _cipher.init(Cipher.ENCRYPT_MODE, _key);
            return _cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("Cannot encrypt data", e);
        }
    }
    
    /**
     * Decrypt a given amount of data.
     * @param data the data to decrypt
     * @return the clear-text data
     * @throws CryptoException if something bad happens
     */
    public synchronized byte[] decrypt(byte[] data) throws CryptoException {
        try {
            _cipher.init(Cipher.DECRYPT_MODE, _key);
            return _cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("Cannot decrypt data", e, data);
        }
    }
}
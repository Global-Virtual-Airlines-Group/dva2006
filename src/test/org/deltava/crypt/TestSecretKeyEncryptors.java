package org.deltava.crypt;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestSecretKeyEncryptors extends TestCase {

    private SecretKeyEncryptor _crypt;
    private static final String _msg = "The Quick Brown Fox jumped over the Lazy Dog";
    
    public static Test suite() {
        return new CoverageDecorator(TestSecretKeyEncryptors.class, new Class[] { DESEncryptor.class,
                BlowfishEncryptor.class } );
    }    
    
    protected void tearDown() throws Exception {
        _crypt = null;
        super.tearDown();
    }

    public void testDES() {
        _crypt = new DESEncryptor("Extra-Super Secret TripleDES Key 123456");
        byte[] code = _crypt.encrypt(_msg.getBytes());
        String result = new String(_crypt.decrypt(code));
        assertEquals(_msg, result);
    }
    
    public void testBlowfish() {
        _crypt = new BlowfishEncryptor("Extra-Super Secret Blowfish Key 123456");
        byte[] code = _crypt.encrypt(_msg.getBytes());
        String result = new String(_crypt.decrypt(code));
        assertEquals(_msg, result);
    }
    
    public void testInvalidKeys() {
        try {
            _crypt = new DESEncryptor("2short");
            fail("CryptoException expected");
        } catch (CryptoException ce) {
        	// empty
        }
        
        try {
            _crypt = new BlowfishEncryptor("2short");
            fail("CryptoException expected");
        } catch (CryptoException ce) {
        	// empty
        }
    }
}
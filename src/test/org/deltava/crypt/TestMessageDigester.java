package org.deltava.crypt;

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

import org.deltava.util.Base64;

public class TestMessageDigester extends TestCase {

    private static final String TESTDATA = "The Quick Brown Fox jumped over the lazy dog";
    
    private MessageDigester _md;
    
    public static Test suite() {
        return new CoverageDecorator(TestMessageDigester.class, new Class[] { MessageDigester.class } );
    }

    protected void tearDown() throws Exception {
        _md = null;
        super.tearDown();
    }

    public void testMD5() {
        _md = new MessageDigester("MD5", 32);
        assertEquals("MD5", _md.getAlgorithm());
        byte[] tData = _md.digest(TESTDATA.getBytes());
        assertNotNull(tData);
    }
    
    public void testSHA1() {
        _md = new MessageDigester("SHA-1", 256);
        assertEquals("SHA-1", _md.getAlgorithm());
        byte[] tData = _md.digest(TESTDATA.getBytes());
        assertNotNull(tData);
    }
    
    public void testSHA256() {
        _md = new MessageDigester("SHA-256");
        _md.salt("salt");
        assertEquals("SHA-256", _md.getAlgorithm());
        byte[] tData = _md.digest(TESTDATA.getBytes());
        assertNotNull(tData);
    }
    
    public void testSHA384() {
        _md = new MessageDigester("SHA-384");
        assertEquals("SHA-384", _md.getAlgorithm());
        byte[] tData = _md.digest(TESTDATA.getBytes());
        assertNotNull(tData);
    }
    
    public void testSHA512() {
        _md = new MessageDigester("SHA-512");
        assertEquals("SHA-512", _md.getAlgorithm());
        byte[] tData = _md.digest(TESTDATA.getBytes());
        assertNotNull(tData);
    }
    
    public void testInputStream() throws Exception {
        _md = new MessageDigester("MD5");
        _md.salt("kosher");
        MessageDigester md2 = new MessageDigester("MD5");
        byte[] tData = _md.digest(new ByteArrayInputStream(TESTDATA.getBytes()));
        byte[] tData2 = md2.digest(TESTDATA.getBytes());
        assertFalse(Base64.encode(tData2).equals(Base64.encode(tData)));
        
        md2.reset();
        md2.salt("kosher");
        tData2 = md2.digest(TESTDATA.getBytes());
        assertEquals(Base64.encode(tData2), Base64.encode(tData));
    }
    
    public void testInvalidAlgorithm() {
        try {
            _md = new MessageDigester("XXXX");
            fail("CryptoException expected");
        } catch (CryptoException ce) { }
    }
}
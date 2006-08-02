package org.deltava.crypt;

import java.io.*;

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
    
    public void testACARSData() throws Exception {
    	_md = new MessageDigester("SHA-256");
    	_md.salt("***REMOVED***");
    	
    	// Load the file and calculate the hash - remember to not load the last two characters
    	File f = new File("data/acars/ACARS Flight O-2006070220.xml");
    	InputStream is = new FileInputStream(f);
    	byte[] data = new byte[(int) f.length()];
    	int size = is.read(data);
    	is.close();
    	assertTrue(size > 10);
    	is = new ByteArrayInputStream(data, 0, size - 2);
    	byte[] tData = _md.digest(is);
    	assertNotNull(tData);
    	
    	// Load the expected value
    	is = new FileInputStream(new File("data/acars/ACARS Flight O-2006070220.sha"));
    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	assertTrue(br.ready());
    	String hash = br.readLine();
    	assertNotNull(hash);
    	byte[] tData2 = MessageDigester.parse(hash);
    	assertNotNull(tData2);
    	
    	// Compare the values
    	assertEquals(tData2.length, tData.length);
    	assertEquals(hash, MessageDigester.convert(tData));
    }
    
    public void testHexConversion() {
    	assertNull(MessageDigester.convert(null));
    	try {
    		byte[] tData = MessageDigester.parse(null);
    		assertNull(tData);
    		fail("IllegalArgumentException expected");
    	} catch (IllegalArgumentException iae) {
    		// empty
    	}
    	
    	try {
    		byte[] tData = MessageDigester.parse("X13");
    		assertNull(tData);
    		fail("IllegalArgumentException expected");
    	} catch (IllegalArgumentException iae) {
    		// empty
    	}
    }
    
    public void testApacheSHA() {
    	_md = new MessageDigester("SHA-1");
    	byte[] tData = _md.digest("password".getBytes());
    	assertEquals("W6ph5Mm5Pz8GgiULbPgzG37mj9g=", Base64.encode(tData));
    }
}
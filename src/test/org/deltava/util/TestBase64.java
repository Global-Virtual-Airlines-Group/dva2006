package org.deltava.util;

import junit.framework.TestCase;

public class TestBase64 extends TestCase {
    
	private static final byte[] enc2 = {1, 2, 4, 5, 6, 7, 8, 11, 127, 126};
    private static final String enc = "The Quick Brown Fox jumped over the 'Lazy Dog'"; 

    public TestBase64() {
        super();
    }

    public void testString() {
        String tmp = Base64.encode(enc);
        assertEquals(enc, new String(Base64.decode(tmp)));
    }
    
    public void testByteArray() {
    	String tmp = Base64.encode(enc2);
    	byte[] enc3 = Base64.decode(tmp);
    	assertEquals(enc2.length, enc3.length);
    	for (int x = 0; x < enc2.length; x++)
    		assertEquals(enc2[x], enc3[x]);
    }
}
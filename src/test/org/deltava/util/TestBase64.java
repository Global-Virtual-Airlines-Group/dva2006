package org.deltava.util;

import java.util.Random;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class TestBase64 extends TestCase {
    
	private static final byte[] enc2 = {1, 2, 4, 5, 6, 7, 8, 11, 127, 126};
    private static final String enc = "The Quick Brown Fox jumped over the 'Lazy Dog'";
    
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private byte[] data;
    
    protected void tearDown() throws Exception {
    	data = null;
    	super.tearDown();
    }

	private void initData() {
		Random rnd = new Random();
		data = new byte[13107200];
    	for (int x = 0; x < data.length; x++)
    		data[x] = (byte) LETTERS.charAt(rnd.nextInt(LETTERS.length()));
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
    
    public void testPerformance() {
    	initData();
    	
    	// Encode using standard Base64
    	long now = System.currentTimeMillis();
    	String enc_1 = Base64.encode(data);
    	assertNotNull(enc_1);
    	enc_1 = null;
    	long time1 = System.currentTimeMillis() - now;
    	System.out.println("Base64 took " + time1 + " ms");
    }
    
    public void testJavaMailPerformance() throws Exception {
    	initData();
    	
    	try {
    		Class<?> c = Class.forName("com.sun.mail.util.BASE64EncoderStream");
    		Method m = c.getDeclaredMethod("encode", byte[].class);
    		
    		long now2 = System.currentTimeMillis();
    		String enc_2 = new String((byte[]) m.invoke(null, data));
    		assertNotNull(enc_2);
    		long time2 = System.currentTimeMillis() - now2;
    		System.out.println("JavaMail took " + time2 + " ms");
    	} catch (NoSuchMethodException nsme) {
    		fail("JavaMail not installed");
    	} catch (ClassNotFoundException cnfe) {
    		fail("JavaMail not installed");
    	}
    }
}
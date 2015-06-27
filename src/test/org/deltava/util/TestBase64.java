package org.deltava.util;

import java.util.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

public class TestBase64 extends TestCase {

	private static final String enc = "The Quick Brown Fox jumped over the 'Lazy Dog'";

	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private byte[] data;

	protected void setUp() throws Exception {
		super.setUp();
		Random rnd = new Random();
		data = new byte[13107200];
		for (int x = 0; x < data.length; x++)
			data[x] = (byte) LETTERS.charAt(rnd.nextInt(LETTERS.length()));
	}

	public void testPerformance() {
		// Encode using standard Base64
		Base64.Encoder b64e = Base64.getEncoder();
		long now = System.currentTimeMillis();
		for (int x = 0; x < 10; x++) {
			String enc_1 = b64e.encodeToString(data);
			assertNotNull(enc_1);
		}

		long time1 = System.currentTimeMillis() - now;
		System.out.println("Base64 took " + time1 + " ms");
		
		Base64.Decoder b64d = Base64.getDecoder();
		String d2 = b64e.encodeToString(enc.getBytes(StandardCharsets.UTF_8));
		assertNotNull(d2);
		assertEquals(enc, new String(b64d.decode(d2), StandardCharsets.UTF_8));
	}

	public void testJavaMailPerformance() throws Exception {
		try {
			Class<?> c = Class.forName("com.sun.mail.util.BASE64EncoderStream");
			Method m = c.getDeclaredMethod("encode", byte[].class);

			long now2 = System.currentTimeMillis();
			for (int x = 0; x < 10; x++) {
				String enc_2 = new String((byte[]) m.invoke(null, data));
				assertNotNull(enc_2);
			}

			long time2 = System.currentTimeMillis() - now2;
			System.out.println("JavaMail took " + time2 + " ms");
		} catch (ClassNotFoundException | NoSuchMethodException nsme) {
			fail("JavaMail not installed");
		}
	}

	public void testCommonsPerformance() throws Exception {
		try {
			Class<?> c = Class.forName("org.apache.commons.codec.binary.Base64");
			Method m = c.getDeclaredMethod("encodeBase64", byte[].class);

			long now3 = System.currentTimeMillis();
			for (int x = 0; x < 10; x++) {
				String enc_3 = new String((byte[]) m.invoke(null, data));
				assertNotNull(enc_3);
			}

			long time3 = System.currentTimeMillis() - now3;
			System.out.println("Commons-codec took " + time3 + " ms");
		} catch (ClassNotFoundException | NoSuchMethodException nsme) {
			fail("Apache commons-codec not installed");
		}
	}

	public void testJDK8() throws Exception {
		try {
			Class<?> c = Class.forName("java.util.Base64");
			Method m = c.getDeclaredMethod("getEncoder", (Class<?>) null);
			Object encoder = m.invoke(null, (Object) null);
			assertNotNull(encoder);
			
			c = encoder.getClass();
			m = c.getDeclaredMethod("encode", byte[].class);
			
			long now3 = System.currentTimeMillis();
			for (int x = 0; x < 10; x++) {
				String enc_3 = new String((byte[]) m.invoke(encoder, data));
				assertNotNull(enc_3);
			}
			
			long time3 = System.currentTimeMillis() - now3;
			System.out.println("JDK8 took " + time3 + " ms");
		} catch (ClassNotFoundException | NoSuchMethodException nsme) {
			fail("JDK8 not installed");
		}
	}
	/*
	public void testJDK8Compatibility() throws Exception {
		try {
			Class<?> c = Class.forName("java.util.Base64");
			assertNotNull(c);
			
			java.util.Base64.Encoder jdk8e = java.util.Base64.getEncoder();
			java.util.Base64.Decoder jdk8d = java.util.Base64.getDecoder();

			String d = jdk8e.encodeToString(enc.getBytes());
			String e2 = Base64.decodeString(d);
			assertEquals(enc, e2);
			
			String d2= Base64.encode(enc.getBytes());
			String e3 = new String(jdk8d.decode(d2));
			assertEquals(enc, e3);
			
			String d3 = jdk8e.encodeToString(data);
			byte[] e4 = Base64.decode(d3);
			assertEquals(data.length, e4.length);
			for (int x = 0; x < data.length; x++)
				assertEquals(data[x], e4[x]);
			
		} catch (ClassNotFoundException cnfe) {
			fail("JDK8 not installed");
		}
	} */
}
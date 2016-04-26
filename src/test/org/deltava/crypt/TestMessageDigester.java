package org.deltava.crypt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import junit.framework.*;

import org.hansel.CoverageDecorator;

public class TestMessageDigester extends TestCase {

	private static final String TESTDATA = "The Quick Brown Fox jumped over the lazy dog";

	private MessageDigester _md;

	public static Test suite() {
		return new CoverageDecorator(TestMessageDigester.class, new Class[] { MessageDigester.class });
	}

	@Override
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
		Base64.Encoder b64e = Base64.getEncoder();
		_md = new MessageDigester("MD5");
		_md.salt("kosher");
		MessageDigester md2 = new MessageDigester("MD5");
		byte[] tData = _md.digest(new ByteArrayInputStream(TESTDATA.getBytes(StandardCharsets.UTF_8)));

		md2.reset();
		md2.salt("kosher");
		byte[] tData2 = md2.digest(TESTDATA.getBytes());
		assertEquals(b64e.encodeToString(tData2), b64e.encodeToString(tData));
	}

	public void testInvalidAlgorithm() {
		try {
			_md = new MessageDigester("XXXX");
			fail("CryptoException expected");
		} catch (CryptoException ce) {
			// empty
		}
	}

	public void testACARSData() throws Exception {
		_md = new MessageDigester("SHA-256");
		_md.salt("ha$h-Salt-ACARS-Value");

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
		br.close();

		// Compare the values
		assertEquals(tData2.length, tData.length);
		assertEquals(hash, MessageDigester.convert(tData));
	}

	@SuppressWarnings("static-method")
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
		Base64.Encoder b64e = Base64.getEncoder();
		_md = new MessageDigester("SHA-1");
		byte[] tData = _md.digest("password".getBytes(StandardCharsets.UTF_8));
		assertEquals("W6ph5Mm5Pz8GgiULbPgzG37mj9g=", b64e.encodeToString(tData));
	}
}
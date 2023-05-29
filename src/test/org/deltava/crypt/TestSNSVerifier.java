package org.deltava.crypt;

import org.json.*;

import java.io.*;

import org.deltava.util.ConfigLoader;

import junit.framework.TestCase;

public class TestSNSVerifier extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
	}

	@SuppressWarnings("static-method")
	public void testSNS() throws Exception {

		JSONObject jo = null;
		try (InputStream is = ConfigLoader.getStream("/data/snsPayload.json")) {
			jo = new JSONObject(new JSONTokener(is));
		}
		
		assertNotNull(jo);
		assertNotNull(SNSVerifier.loadCertificate(jo.getString("SigningCertURL")));
		assertTrue(SNSVerifier.validate(jo));
	}
}
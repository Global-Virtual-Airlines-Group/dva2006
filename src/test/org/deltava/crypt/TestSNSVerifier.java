package org.deltava.crypt;

import org.json.*;

import java.io.InputStream;

import org.apache.log4j.*;
import org.deltava.util.ConfigLoader;

import junit.framework.TestCase;

public class TestSNSVerifier extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
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
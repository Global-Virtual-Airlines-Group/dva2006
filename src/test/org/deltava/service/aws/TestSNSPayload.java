package org.deltava.service.aws;

import java.io.InputStream;

import org.apache.log4j.*;
import org.deltava.crypt.SNSVerifier;
import org.deltava.util.ConfigLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import junit.framework.TestCase;

public class TestSNSPayload extends TestCase {

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
	public void testSNSConfirm() throws Exception {
		
		JSONObject jo = null;
		try (InputStream is = ConfigLoader.getStream("/data/snsPayload.json")) {
			jo = new JSONObject(new JSONTokener(is));
		}
		
		assertNotNull(jo);
		assertNotNull(SNSVerifier.loadCertificate(jo.getString("SigningCertURL")));
		assertTrue(SNSVerifier.validate(jo));
		
		SNSPayload sp = new SNSPayload(jo.getString("MessageId"), jo.getString("TopicArn"), jo.getString("Type"));
		sp.setBody(jo);
		
		assertNotNull(sp.getBody());
		assertEquals(jo.getString("MessageId"), sp.getID());
		assertEquals(jo.getString("TopicArn"), sp.getTopic());
		assertEquals(jo.getString("Type"), sp.getType());
	}

	@SuppressWarnings("static-method")
	public void testSESDelivery() throws Exception {
		
		JSONObject jo = null;
		try (InputStream is = ConfigLoader.getStream("/data/snsPayload.ses.json")) {
			jo = new JSONObject(new JSONTokener(is));
		}
		
		assertNotNull(jo);
		assertNotNull(SNSVerifier.loadCertificate(jo.getString("SigningCertURL")));
		assertTrue(SNSVerifier.validate(jo));
		
		SNSPayload sp = new SNSPayload(jo.getString("MessageId"), jo.getString("TopicArn"), jo.getString("Type"));
		sp.setBody(jo);
		
		assertEquals(jo.getString("MessageId"), sp.getID());
		assertEquals(jo.getString("TopicArn"), sp.getTopic());
		assertEquals(jo.getString("Type"), sp.getType());
		
		JSONObject mo = new JSONObject(jo.getString("Message"));
		assertNotNull(mo);
	}
}
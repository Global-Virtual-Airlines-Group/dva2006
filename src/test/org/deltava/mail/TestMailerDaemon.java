package org.deltava.mail;

import org.apache.log4j.*;

import org.json.*;

import org.deltava.beans.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestMailerDaemon extends TestCase {
	
	private MailerDaemon _md;
	private Thread _pdt;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		
		SystemData.init("org.deltava.util.system.EMailTestSystemDataLoader", true);
		
		_md = new MailerDaemon();
		assertNotNull(_md);
		_pdt = new Thread(_md);
		assertNotNull(_pdt);
		_pdt.setDaemon(true);
		_pdt.start();
		assertTrue(_pdt.isAlive());
		assertTrue(_pdt.isDaemon());
	}

	@Override
	protected void tearDown() throws Exception {
		ThreadUtils.kill(_pdt, 500);
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSendPush() throws Exception {
		assertFalse(true);
		
		// Build the Endpoint
		PushEndpoint ep = new PushEndpoint(8027, "https://fcm.googleapis.com/fcm/send/eg7uGW-1E-k:APA91bF60FXSuDcwCBOdoUbRv-Gn-wvpzFBdHK-RmV3GENOt7rnzOauss-BM1kefFY7vydU-0smH2Nio_eKc6e7J-9WiC-u3UK8A8HV7KtITFnGVUXRFqe4HkX2D2XPpcx9GGAHhU8Di");
		ep.setAuth("CY1onnjlFH99vwwMVoFGSw");
		ep.setPub256DH("BCmaZK4ID1ygzg-Wsjs_grUwAv2filUO-tiuEPGD9jBOyS-9isDT8MIxy2hvxD7jRNDJIRjwCmh55DY3JlAw_E0");
		
		// Build the message object
		VAPIDEnvelope env = new VAPIDEnvelope(ep);
		JSONObject mo = new JSONObject();
		mo.put("title", "Notification Title");
		mo.put("body", "Notification Message Body - stuff is put here");
		mo.put("lang", "en");
		mo.put("requireInteraction", true);
		mo.put("url", String.format("https://%s/pirepqueue.do", SystemData.get("airline.url")));
		mo.put("icon", String.format("/%s/favicon/favicon-32x32.png", SystemData.get("path.img")));
	
		JSONArray ma = new JSONArray();
		ma.put(new JSONObject("{action:\"pilotcenter\",title:\"Pilot Center\"}"));
		ma.put(new JSONObject("{action:\"pirepqueue\",title:\"Flight Queue\"}"));
		mo.put("actions", ma);
		
		env.setBody(mo.toString());
		
		// Convert to VAPID
		assertTrue(_pdt.isAlive());
		MailerDaemon.push(env);
		
		// Wait
		Thread.sleep(5000);
		
		assertTrue(_md.getInvalidEndpoints().isEmpty());
	}
	
	public void testSendEMail() throws Exception {
		
		EMailAddress addr = MailUtils.makeAddress("luke@deltava.org", "Luke Kolin");
		assertNotNull(addr);
		SMTPEnvelope env = new SMTPEnvelope(true, addr);
		env.setSubject("Test EMail");
		env.setBody("Test message body");
		env.setContentType("text/plain");
		env.setRecipient(MailUtils.makeAddress("luke@sce.net", "Luke Kolin"));

		// Convert to SMTP
		assertTrue(_pdt.isAlive());
		MailerDaemon.push(env);
		
		Thread.sleep(5000);
	}
}
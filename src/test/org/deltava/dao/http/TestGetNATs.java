package org.deltava.dao.http;

import java.net.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.dao.DAOException;
import org.deltava.util.http.SSLUtils;

public class TestGetNATs extends TestCase {

	private GetNATs _dao;
	private HttpsURLConnection _con;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
		
		URL url = new URL("https://www.notams.jcs.mil/common/nat.html");
		_con = (HttpsURLConnection) url.openConnection();
		assertNotNull(_con);
		_dao = new GetNATs(_con);
		assertNotNull(_dao);
	}

	protected void tearDown() throws Exception {
		_dao = null;
		_con.disconnect();
		_con = null;
		super.tearDown();
	}

	public void testInvalidKeyStore() {
		try {
			String natInfo = _dao.getTrackInfo();
			assertNull(natInfo);
			fail("DAOException expected");
		} catch (DAOException de) {
			// valid
		}
	}
	
	public void testValidKeyStore() throws Exception {
		
		// Load the certificate Init the SSL context
		X509Certificate cert = SSLUtils.load("/etc/jcs.notams.cer");
		assertNotNull(cert);
		SSLContext ctx = SSLUtils.getContext(cert);
		assertNotNull(ctx);
		
		// Set the socket factory
		_con.setSSLSocketFactory(ctx.getSocketFactory());
		
		// Pull down the data
		String natInfo = _dao.getTrackInfo();
		assertNotNull(natInfo);
	}
}
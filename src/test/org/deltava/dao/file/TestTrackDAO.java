package org.deltava.dao.file;

import java.net.*;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.util.http.SSLUtils;

public class TestTrackDAO extends TestCase {

	private TrackDAO _dao;
	private HttpsURLConnection _con;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
	}
	
	private void init(String url) {
		try {
			URL u = new URL(url);
			_con = (HttpsURLConnection) u.openConnection();
			assertNotNull(_con);
		} catch (MalformedURLException mue) {
			fail("Invalid URL - " + url);
		} catch (IOException ie) {
			fail("I/O error - " + ie.getMessage());
		}
	}

	protected void tearDown() throws Exception {
		_dao = null;
		_con.disconnect();
		_con = null;
		super.tearDown();
	}

	public void testInvalidKeyNAT() {
		init("https://www.notams.jcs.mil/common/nat.html");
		try {
			_dao = new GetNATs(_con.getInputStream());
			String natInfo = _dao.getTrackInfo();
			assertNull(natInfo);
			fail("DAOException expected");
		} catch (Exception e) {
			// valid
		}
	}
	
	public void testValidKeyNAT() throws Exception {
		init("https://www.notams.jcs.mil/common/nat.html");
		
		// Load the certificate Init the SSL context
		X509Certificate cert = SSLUtils.load("/etc/jcs.notams.cer");
		assertNotNull(cert);
		SSLContext ctx = SSLUtils.getContext(cert);
		assertNotNull(ctx);
		
		// Set the socket factory
		_con.setSSLSocketFactory(ctx.getSocketFactory());
		
		// Pull down the data
		_dao = new GetNATs(_con.getInputStream());
		String natInfo = _dao.getTrackInfo();
		assertNotNull(natInfo);
	}
	
	public void testInvalidKeyPACOT() {
		init("https://www.notams.jcs.mil/tracks/pTracks.html?advsw=PAC");
		try {
			_dao = new GetPACOTs(_con.getInputStream());
			String pacotInfo = _dao.getTrackInfo();
			assertNull(pacotInfo);
			fail("DAOException expected");
		} catch (Exception e) {
			// valid
		}
	}
	
	public void testValidKeyPACOT() throws Exception {
		init("https://www.notams.jcs.mil/tracks/pTracks.html?advsw=PAC");
		
		// Load the certificate Init the SSL context
		X509Certificate cert = SSLUtils.load("/etc/jcs.notams.cer");
		assertNotNull(cert);
		SSLContext ctx = SSLUtils.getContext(cert);
		assertNotNull(ctx);
		
		// Set the socket factory
		_con.setSSLSocketFactory(ctx.getSocketFactory());

		// Pull down the data
		_dao = new GetPACOTs(_con.getInputStream());
		String pacotInfo = _dao.getTrackInfo();
		assertNotNull(pacotInfo);
	}
}
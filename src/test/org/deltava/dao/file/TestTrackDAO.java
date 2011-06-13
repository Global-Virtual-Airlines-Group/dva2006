package org.deltava.dao.file;

import java.util.*;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.dao.http.*;
import org.deltava.util.http.SSLUtils;

public class TestTrackDAO extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
	}
	
	public void testValidKeyNAT() throws Exception {
		
		// Load the certificate Init the SSL context
		X509Certificate cert = SSLUtils.load("/etc/jcs.notams.cer");
		assertNotNull(cert);
		SSLContext ctx = SSLUtils.getContext(cert);
		assertNotNull(ctx);
		
		// Pull down the data
		GetNATs dao = new GetNATs("https://www.notams.faa.gov/common/nat.html");
		//dao.setSSLContext(ctx);

		String natInfo = dao.getTrackInfo();
		assertNotNull(natInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
	}
	
	public void testValidKeyPACOT() throws Exception {
		
		// Load the certificate Init the SSL context
		X509Certificate cert = SSLUtils.load("/etc/jcs.notams.cer");
		assertNotNull(cert);
		SSLContext ctx = SSLUtils.getContext(cert);
		assertNotNull(ctx);
		
		// Pull down the data
		GetPACOTs dao = new GetPACOTs("https://www.notams.faa.gov/dinsQueryWeb/advancedNotamMapAction.do?queryType=pacificTracks");
		//dao.setSSLContext(ctx);
		String pacotInfo = dao.getTrackInfo();
		assertNotNull(pacotInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
	}
	
	public void testAUSOT() throws Exception {
		
		// Pull down the data
		GetAUSOTs dao = new GetAUSOTs("http://www.airservicesaustralia.com/customer/ausots/html.asp?/flextracks/text.asp?ver=1");
		String ausotInfo = dao.getTrackInfo();
		assertNotNull(ausotInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
	}
}
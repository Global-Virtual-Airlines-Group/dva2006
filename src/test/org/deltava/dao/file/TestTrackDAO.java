package org.deltava.dao.file;

import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.dao.http.*;

@SuppressWarnings("static-method")
public class TestTrackDAO extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
	}
	
	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	public void testNAT() throws Exception {
		
		// Pull down the data
		GetNATs dao = new GetNATs("https://www.notams.faa.gov/common/nat.html");
		String natInfo = dao.getTrackInfo();
		assertNotNull(natInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
		assertFalse(results.isEmpty());
	}
	
	public void testPACOT() throws Exception {
		
		// Pull down the data
		GetPACOTs dao = new GetPACOTs("https://www.notams.faa.gov/dinsQueryWeb/advancedNotamMapAction.do?queryType=pacificTracks");
		String pacotInfo = dao.getTrackInfo();
		assertNotNull(pacotInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
		assertFalse(results.isEmpty());
	}
	
	public void testAUSOT() throws Exception {
		
		// Pull down the data
		GetAUSOTs dao = new GetAUSOTs("http://www.airservicesaustralia.com/customer/ausots/html.asp?/flextracks/text.asp?ver=1");
		String ausotInfo = dao.getTrackInfo();
		assertNotNull(ausotInfo);
		
		// Test that we can parse
		Map<String, Collection<String>> results = dao.getWaypoints();
		assertNotNull(results);
		assertFalse(results.isEmpty());
	}
}
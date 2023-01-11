package org.deltava.dao.http;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.dao.DAOException;

public class TestGetOceanic extends TestCase {

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
	public void testNAT() throws DAOException {
		GetNATs dao = new GetNATs("https://www.notams.faa.gov/common/nat.html");
		String info = dao.getTrackInfo();
		assertNotNull(info);
		assertTrue(info.length() > 0);
		 Map<String, Collection<String>> trackInfo = dao.getWaypoints();
		 assertNotNull(trackInfo);
		 assertFalse(trackInfo.isEmpty());
	}
	
	@SuppressWarnings("static-method")
	public void testPACOT() throws DAOException {
		GetPACOTs dao = new GetPACOTs("https://www.notams.faa.gov/dinsQueryWeb/advancedNotamMapAction.do?queryType=pacificTracks&actionType=advancedNOTAMFunctions");
		String info = dao.getTrackInfo();
		assertNotNull(info);
		assertTrue(info.length() > 0);
		Map<String, Collection<String>> trackInfo = dao.getWaypoints();
		 assertNotNull(trackInfo);
		 assertFalse(trackInfo.isEmpty());
	}
	
	@SuppressWarnings("static-method")
	public void testAUSOT() throws DAOException {
		GetAUSOTs dao = new GetAUSOTs("http://www.airservicesaustralia.com/flextracks/text.asp?ver=1");
		String info = dao.getTrackInfo();
		assertNotNull(info);
		assertTrue(info.length() > 0);
	}
}
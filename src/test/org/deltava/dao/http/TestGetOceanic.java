package org.deltava.dao.http;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.deltava.dao.DAOException;

public class TestGetOceanic extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
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
		GetAUSOTs dao = new GetAUSOTs("https://www.airservicesaustralia.com/flextracks/text.asp?ver=1");
		String info = dao.getTrackInfo();
		assertNotNull(info);
		assertTrue(info.length() > 0);
	}
}
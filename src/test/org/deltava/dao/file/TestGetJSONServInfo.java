package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.NetworkInfo;
import org.deltava.beans.servinfo.RadioPosition;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestGetJSONServInfo extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "14072")) {
			assertNotNull(c);
		
		// Load the airports/time zones
			GetTimeZone tzdao = new GetTimeZone(c);
			tzdao.initAll();
			GetAirport apdao = new GetAirport(c);
			SystemData.add("airports", apdao.getAll());
			GetAirline aldao = new GetAirline(c);
			SystemData.add("airlines", aldao.getAll());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testLoadVATSIM() throws Exception {
		
		File ft = new File("data", "transceivers-data.json");
		assertTrue(ft.exists());
		
		Collection<RadioPosition> positions = new ArrayList<RadioPosition>();
		try (InputStream is = new BufferedInputStream(new FileInputStream(ft), 65536)) {
			GetVATSIMTransceivers tdao = new GetVATSIMTransceivers(is);
			positions.addAll(tdao.load());
		}
		
		assertFalse(positions.isEmpty());
		File f3 = new File("data", "vatsim-data.json");
		assertTrue(f3.exists());
		
		try (InputStream is = new BufferedInputStream(new FileInputStream(f3), 102400)) {
			GetVATSIMInfo dao = new GetVATSIMInfo(is);
			NetworkInfo inf = dao.getInfo();
			assertNotNull(inf);
			assertEquals(OnlineNetwork.VATSIM, inf.getNetwork());
			assertFalse(inf.getServers().isEmpty());
			assertFalse(inf.getPilots().isEmpty());
			assertFalse(inf.getControllers().isEmpty());
			inf.merge(positions);
		}
	}
	
	@SuppressWarnings("static-method")
	public void testLoadIVAO() throws Exception {
		
		File f = new File("data", "ivao-data.json");
		assertTrue(f.exists());
		
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 102400)) {
			GetIVAOInfo dao = new GetIVAOInfo(is);
			NetworkInfo inf = dao.getInfo();
			assertNotNull(inf);
			assertEquals(OnlineNetwork.IVAO, inf.getNetwork());
			assertFalse(inf.getServers().isEmpty());
			assertFalse(inf.getPilots().isEmpty());
			assertFalse(inf.getControllers().isEmpty());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testLoadPOSCON() throws Exception {
		
		File f = new File("data", "poscon-data.json");
		assertTrue(f.exists());
		
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 102400)) {
			GetPOSCONInfo dao = new GetPOSCONInfo(is);
			NetworkInfo inf = dao.getInfo();
			assertNotNull(inf);
			assertEquals(OnlineNetwork.POSCON, inf.getNetwork());
			assertTrue(inf.getServers().isEmpty());
			assertFalse(inf.getPilots().isEmpty());
			assertFalse(inf.getControllers().isEmpty());
		}
	}
}
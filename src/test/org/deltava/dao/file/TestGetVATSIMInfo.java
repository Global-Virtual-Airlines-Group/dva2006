package org.deltava.dao.file;

import java.io.*;
import java.sql.*;

import org.apache.log4j.*;

import org.deltava.beans.servinfo.NetworkInfo;

import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestGetVATSIMInfo extends TestCase {
	
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
	public void testLoad() throws Exception {
		
		File f = new File("data", "vatsim-data.json");
		assertTrue(f.exists());
		
		try (InputStream is = new FileInputStream(f)) {
			GetVATSIMInfo dao = new GetVATSIMInfo(is);	
			NetworkInfo inf = dao.getInfo();
			assertNotNull(inf);
		}
	}
}
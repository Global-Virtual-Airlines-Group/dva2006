package org.deltava.service.simfdr;

import java.io.*;
import java.sql.*;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.SimFDRFlightReport;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestOfflineFlightParser extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
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
	public void testParser() throws Exception {
		
		File f = new File("data", "simfdr_flight_export.xml");
		assertTrue(f.exists());
		
		StringBuilder buf = new StringBuilder();
		try (BufferedReader isr = new BufferedReader(new FileReader(f))) {
			String data = null;
			while ((data = isr.readLine()) != null) {
				buf.append(data);
				buf.append("\r\n");
			}
		}
		
		OfflineFlight<SimFDRFlightReport, ACARSRouteEntry> ofr = OfflineFlightParser.create(buf.toString());
		assertNotNull(ofr);
		assertNotNull(ofr.getFlightReport());
		assertFalse(ofr.getPositions().isEmpty());
	}
}
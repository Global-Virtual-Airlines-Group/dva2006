// Copyright 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.File;
import java.sql.*;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.wx.*;
import org.deltava.beans.navdata.AirportLocation;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class TestGetFAWeather extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?connectionTimezone=SERVER&allowPublicKeyRetrieval=true";
	private static final String KEY = "foo";
	
	private static Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestGetFAWeather.class);
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
			assertNotNull(c);
		
			// Initialize System Data
			SystemData.init();

			// Load Time Zones
			GetTimeZone dao = new GetTimeZone(c);
			dao.initAll();
			log.info("Loaded Time Zones");
			
			// Load country codes
			log.info("Loading Country codes");
			GetCountry cdao = new GetCountry(c);
			log.info("Loaded {} Country codes", Integer.valueOf(cdao.initAll()));

			// Load Database information
			log.info("Loading Cross-Application data");
			GetUserData uddao = new GetUserData(c);
			SystemData.add("apps", uddao.getAirlines(true));

			// Load active airlines
			log.info("Loading Airline Codes");
			GetAirline aldao = new GetAirline(c);
			SystemData.add("airlines", aldao.getAll());

			// Load airports
			log.info("Loading Airports");
			GetAirport apdao = new GetAirport(c);
			SystemData.add("airports", apdao.getAll());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testMETAR() throws Exception {
		
		GetFAWeather fadao = new GetFAWeather();
		fadao.setReadTimeout(7500);
		fadao.setKey(KEY);
		
		METAR m = fadao.getMETAR(new AirportLocation(SystemData.getAirport("EGLL")));
		assertNotNull(m);
		assertEquals("EGLL", m.getCode());
		fadao.reset();
		
		m = fadao.getMETAR(new AirportLocation(SystemData.getAirport("KSEA")));
		assertNotNull(m);
		assertEquals("KSEA", m.getCode());
	}
	
	@SuppressWarnings("static-method")
	public void testTAF() throws Exception {

		GetFAWeather fadao = new GetFAWeather();
		fadao.setReadTimeout(7500);
		fadao.setKey(KEY);

		TAF t = fadao.getTAF(new AirportLocation(SystemData.getAirport("EGLL")));
		assertNotNull(t);
		assertEquals("EGLL", t.getCode());
		fadao.reset();
		
		t = fadao.getTAF(new AirportLocation(SystemData.getAirport("KSEA")));
		assertNotNull(t);
		assertEquals("KSEA", t.getCode());
	}
}
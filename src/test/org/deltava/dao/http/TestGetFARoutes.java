// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.File;
import java.sql.*;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class TestGetFARoutes extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";
	
	private static Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestGetFARoutes.class);
		
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
			log.info("Loaded " + cdao.initAll() + " Country codes");

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
	public void testRoutes() throws Exception {
		
		GetFARoutes fadao = new GetFARoutes();
		fadao.setReadTimeout(7500);
		fadao.setKey(SystemData.get("schedule.flightaware.flightXML.v4"));
		
		RoutePair rp = RoutePair.of(SystemData.getAirport("KCVG"), SystemData.getAirport("KATL"));
		Collection<ExternalRoute> routes = fadao.getRouteData(rp);
		assertNotNull(routes);
		assertFalse(routes.isEmpty());
	}
}
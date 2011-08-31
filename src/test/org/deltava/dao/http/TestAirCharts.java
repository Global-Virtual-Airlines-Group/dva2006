// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestAirCharts extends TestCase {
	
	private Logger log;
	
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(TestAirCharts.class);
		
		Country.init("US", "United States");
		SystemData.init();
		
		assertNotNull(Country.get("US"));
		assertNotNull(SystemData.get("security.key.airCharts"));
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testChartList() throws DAOException {
		
		GetAirCharts acdao = new GetAirCharts();
		Collection<Country> countries = acdao.getCountries();
		assertNotNull(countries);
		log.info("Loaded " + countries.size() + " countries");
		assertTrue(countries.contains(Country.get("US")));
		
		// Get airports
		Collection<Airport> airports = acdao.getAirports(Country.get("US"));
		assertNotNull(airports);
	}
}

// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.TZInfo;
import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestAirCharts extends TestCase {
	
	private Logger log;
	
	//private Airport _atl;
	private Airport _tlv;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(TestAirCharts.class);
		
		Country.init("US", "United States", "North America");
		Country.init("IL", "Israel", "Asia");
		SystemData.init();
		
		final Airport a = new Airport("ATL", "KATL", "Atlanta GA");
		a.setCountry(Country.get("US"));
		a.setTZ(TZInfo.local());
		//_atl = a;
		final Airport a2 = new Airport("TLV", "LLBG", "Tel Aviv Israel");
		a.setCountry(Country.get("IL"));
		a2.setTZ(TZInfo.local());
		_tlv = a2;
		Map<String, Airport> airports = new HashMap<String, Airport>() {{ put("KATL", a); put("ATL", a); put("LLBG", a2); put("TLV", a2); }};
		SystemData.add("airports", airports);
		
		assertNotNull(Country.get("US"));
		assertNotNull(Country.get("IL"));
		assertNotNull(airports.get("KATL"));
		assertNotNull(airports.get("LLBG"));
		assertNotNull(SystemData.get("security.key.airCharts"));
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	public void testCountryList() throws DAOException {
		
		GetAirCharts acdao = new GetAirCharts();
		Collection<Country> countries = acdao.getCountries();
		assertNotNull(countries);
		log.info("Loaded " + countries.size() + " countries");
		assertTrue(countries.contains(Country.get("US")));
		assertTrue(countries.contains(Country.get("IL")));
	}
	
	public void testAirportList() throws DAOException {
		
		GetAirCharts acdao = new GetAirCharts();
		Collection<Airport> airports = acdao.getAirports(Country.get("IL"));
		assertNotNull(airports);
		log.info("Loaded " + airports.size() + " airports");
		assertTrue(airports.contains(_tlv));
	}
	
	public void testChartList() throws DAOException {
		
		GetAirCharts acdao = new GetAirCharts();
		Collection<ExternalChart> charts = acdao.getCharts(_tlv);
		assertNotNull(charts);
		log.info("Loaded " + charts.size() + " charts");
		
		// Populate charts
		GetExternalCharts exdao = new GetExternalCharts();
		for (ExternalChart ec : charts) {
			long start = System.currentTimeMillis();	
			exdao.populate(ec);
			long now = System.currentTimeMillis();
			log.info("Populated " + ec.getName() + " in " + (now - start) + "ms");
		}
	}
}
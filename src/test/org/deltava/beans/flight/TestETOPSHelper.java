// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class TestETOPSHelper extends AbstractDAOTestCase {
	
	private Logger log;
	
	private GetACARSData _fdao;
	private GetAircraft _acdao;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(TestETOPSHelper.class);
		log.info("Starting");
		
		// Init SystemData
		SystemData.init();
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_con);
		tzdao.initAll();
		GetCountry cdao = new GetCountry(_con);
		cdao.initAll();
		GetUserData uddao = new GetUserData(_con);
		SystemData.add("apps", uddao.getAirlines(true));
		GetAirport apdao = new GetAirport(_con);
		Map<String, Airport> airports = apdao.getAll();
		SystemData.add("airports", airports);
		GetAirline aldao = new GetAirline(_con);
		SystemData.add("airlines", aldao.getAll());
		ETOPSHelper.init(apdao.getAll().values());
		
		// Init DAOs
		_fdao = new GetACARSData(_con);
		_acdao = new GetAircraft(_con);
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	public void testAirports() throws DAOException {
		
		Aircraft a = _acdao.get("B737-700");
		assertNotNull(a);
		
		Airport ams = SystemData.getAirport("AMS");
		Airport bhx = SystemData.getAirport("BHX");
		ScheduleRoute rt = new ScheduleRoute(SystemData.getAirline("DVA"),ams, bhx);
		
		assertFalse(ETOPSHelper.validate(a, rt));
	}

	public void testShortRange() throws DAOException {
		
		FlightInfo fi = _fdao.getInfo(627783);
		assertNotNull(fi);
		Aircraft a = _acdao.get(fi.getEquipmentType());
		assertNotNull(a);
		
		Collection<? extends RouteEntry> entries = _fdao.getRouteEntries(fi.getID(), fi.getArchived());
		assertNotNull(entries);
		assertTrue(entries.size() > 3);
	
		// Validate
		assertFalse(ETOPSHelper.validate(a, fi));
		assertEquals(ETOPS.ETOPS60, ETOPSHelper.classify(entries));
		
		fi = _fdao.getInfo(621208);
		assertNotNull(fi);
		a = _acdao.get("B737-200");
		assertNotNull(a);
		
		entries = _fdao.getRouteEntries(fi.getID(), fi.getArchived());
		assertNotNull(entries);
		assertTrue(entries.size() > 3);

		// Validate
		assertFalse(ETOPSHelper.validate(a, fi));
		assertEquals(ETOPS.ETOPS60, ETOPSHelper.classify(entries));
	}
	
	public void testPolar() throws DAOException {

		FlightInfo fi = _fdao.getInfo(622286);
		assertNotNull(fi);
		Aircraft a = _acdao.get("B737-200");
		assertNotNull(a);
		
		Collection<? extends RouteEntry> entries = _fdao.getRouteEntries(fi.getID(), fi.getArchived());
		assertNotNull(entries);
		assertTrue(entries.size() > 3);

		// Validate
		assertTrue(ETOPSHelper.validate(a, fi));
		assertEquals(ETOPS.ETOPS180, ETOPSHelper.classify(entries));
	}
	
	public void testPacific() throws DAOException {
		
		FlightInfo fi = _fdao.getInfo(630107);
		assertNotNull(fi);
		Aircraft a = _acdao.get("B737-200");
		assertNotNull(a);
		
		Collection<? extends RouteEntry> entries = _fdao.getRouteEntries(fi.getID(), fi.getArchived());
		assertNotNull(entries);
		assertTrue(entries.size() > 3);
		
		// Validate
		assertTrue(ETOPSHelper.validate(a, fi));
		assertNull(ETOPSHelper.classify(entries));
	}
	
	public void testAtlantic() throws DAOException {
		
		// 310447, 634308
		FlightInfo fi = _fdao.getInfo(310447);
		assertNotNull(fi);
		Aircraft a = _acdao.get("B737-200");
		assertNotNull(a);

		Collection<? extends RouteEntry> entries = _fdao.getRouteEntries(fi.getID(), fi.getArchived());
		assertNotNull(entries);
		assertTrue(entries.size() > 3);

		// Validate
		assertTrue(ETOPSHelper.validate(a, fi));
		assertEquals(ETOPS.ETOPS120, ETOPSHelper.classify(entries));
	}
}
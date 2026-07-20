package org.deltava.dao.http;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;

import junit.framework.TestCase;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

public class TestGetAviationStack extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true";
	
	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());

		CacheManager.init("TEST");
		SystemData.init();

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "14072");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		// Get EQ types
		GetAircraft acdao = new GetAircraft(_c);
		_acTypes.addAll(acdao.getAircraftTypes());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}
	
	private static void validateFlights(Collection<RawScheduleEntry> entries) {
		for (RawScheduleEntry rse : entries) {
			assertNotNull(rse);
			assertEquals(ScheduleSource.AVSTACK, rse.getSource());
			assertNotNull(rse.getAirline());
			assertNotNull(rse.getAirportD());
			assertNotNull(rse.getAirportA());
			assertTrue(rse.getFlightNumber() > 0);
			assertEquals(1, rse.getLeg());
			assertNotNull(rse.getEquipmentType());
			assertNotNull(rse.getTimeD());
			assertNotNull(rse.getTimeA());
			assertTrue(rse.getTimeD().isBefore(rse.getTimeA()));
			assertEquals(1, rse.getDays().size());
		}
	}

	public void testLoad() throws Exception {
		
		LocalDate dt = LocalDate.now().plusDays(7);
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffATL_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("ATL"), SystemData.getAirline("DL"), dt, true);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffJFK_dl_a.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, true);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffJFK_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, false);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffCDG_af_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, true);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffCDG_af_a.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, false);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
	}
}
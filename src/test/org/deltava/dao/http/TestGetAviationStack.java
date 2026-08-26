package org.deltava.dao.http;

import java.io.*;
import java.time.*;

import org.deltava.ScheduleTestCase;

import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

public class TestGetAviationStack extends ScheduleTestCase {
	
	public void testLoad() throws Exception {
		
		LocalDate dt = LocalDate.now().plusDays(14);
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffATL_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("ATL"), SystemData.getAirline("DL"), dt, true, 0);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			assertEquals(results.getTotal(), results.getCount());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffJFK_dl_a.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, true, 0);
			assertNotNull(results);
			assertEquals(results.getTotal(), results.getCount());
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffJFK_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, false, 0);
			assertNotNull(results);
			assertEquals(results.getTotal(), results.getCount());
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffCDG_af_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, true, 0);
			assertNotNull(results);
			assertEquals(results.getTotal(), results.getCount());
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffCDG_af_a.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, false, 0);
			assertNotNull(results);
			assertEquals(results.getTotal(), results.getCount());
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
		
		// Test times with seconds
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffBOG_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("BOD"), SystemData.getAirline("DL"), dt, true, 0);
			assertNotNull(results);
			assertEquals(results.getTotal(), results.getCount());
			assertFalse(results.isEmpty());
			assertTrue(results.size() <= results.getCount());
			assertTrue(results.size() <= results.getTotal());
			validateFlights(results);
		}
	}
	
	public void testEmpty() throws Exception {

		LocalDate dt = LocalDate.now().plusDays(14);
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffPVR_ws_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("PVR"), SystemData.getAirline("AF"), dt, false, 0);
			assertNotNull(results);
			assertEquals(0, results.getCount());
			assertEquals(results.getTotal(), results.getCount());
			assertTrue(results.isEmpty());
		}
	}
}
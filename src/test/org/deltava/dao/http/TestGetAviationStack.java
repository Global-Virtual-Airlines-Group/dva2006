package org.deltava.dao.http;

import java.io.*;
import java.time.*;
import java.util.*;

import org.deltava.ScheduleTestCase;

import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

public class TestGetAviationStack extends ScheduleTestCase {
	
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
		
		LocalDate dt = LocalDate.now().plusDays(14);
		try (InputStream is = ConfigLoader.getStream("/data/avstack/ffATL_dl_d.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("ATL"), SystemData.getAirline("DL"), dt, true);
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
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, true);
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
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("JFK"), SystemData.getAirline("DL"), dt, false);
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
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, true);
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
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, false);
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
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("CDG"), SystemData.getAirline("AF"), dt, false);
			assertNotNull(results);
			assertEquals(0, results.getCount());
			assertEquals(results.getTotal(), results.getCount());
			assertTrue(results.isEmpty());
		}
	}
}
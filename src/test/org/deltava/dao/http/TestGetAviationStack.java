package org.deltava.dao.http;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

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
	
	public void testCodeShares() throws Exception {
	
		LocalDate dt = LocalDate.now().plusDays(14);
		List<RawScheduleEntry> allFlights = new ArrayList<RawScheduleEntry>();
		try (InputStream is = ConfigLoader.getStream("/data/avstack/lax_cs_dl.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("LAX"), SystemData.getAirline("DL"), dt, true, 0);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			allFlights.addAll(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/lax_cs_af.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("LAX"), SystemData.getAirline("AF"), dt, true, 0);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			allFlights.addAll(results);
		}

		// Search for codeshares
		List<RawScheduleEntry> csEntries = allFlights.stream().filter(ScheduleEntry::isCodeShare).collect(Collectors.toList());
		assertFalse(csEntries.isEmpty());
		assertEquals(2, csEntries.stream().filter(rse -> "VS024".equals(rse.getShortCode())).count());
			
		// Merge code shares
		int oldSize = allFlights.size();
		RawScheduleHelper.mergeCodeShares(allFlights);
		assertTrue(allFlights.size() < oldSize);
		assertEquals(1, allFlights.stream().filter(rse -> "VS024".equals(rse.getShortCode())).count());
		assertTrue(allFlights.stream().filter(rse -> "MULTI".equals(rse.getCodeShare())).findAny().isPresent());
		
		// Set line numbers
		RawScheduleHelper.calculateLineNumbers(allFlights);
	}
}
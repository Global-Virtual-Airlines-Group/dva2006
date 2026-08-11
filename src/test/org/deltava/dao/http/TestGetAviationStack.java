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
			
		// Merge code shares, allowing DL and AF
		int oldSize = allFlights.size();
		RawScheduleHelper.mergeCodeShares(allFlights, new CodeShareFilter(Set.of("DL","AF")));
		assertTrue(allFlights.size() < oldSize);
		assertFalse(allFlights.isEmpty());
		
		// Test multi-code shares
		List<RawScheduleEntry> mcs = allFlights.stream().filter(rse -> ScheduleEntry.MULTI_CS.equals(rse.getCodeShare())).collect(Collectors.toList());
		Optional<RawScheduleEntry> ose = mcs.stream().filter(rse -> "VS024".equals(rse.getShortCode())).findAny();
		assertTrue(ose.isPresent());
		
		// Test multi code-share
		RawScheduleEntry rse = ose.get();
		Collection<String> alCodes = rse.getCodeShareOperators();
		assertEquals(2, alCodes.size());
		assertTrue(alCodes.contains("DL"));
		assertTrue(alCodes.contains("AF"));
		
		// Set line numbers
		RawScheduleHelper.calculateLineNumbers(allFlights);
	}
	
	public void testCodeShareFilter() throws Exception {
		
		LocalDate dt = LocalDate.now().plusDays(14);
		List<RawScheduleEntry> allFlights = new ArrayList<RawScheduleEntry>();
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/lax_cs_kl.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("LAX"), SystemData.getAirline("KL"), dt, true, 0);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			allFlights.addAll(results);
		}
		
		try (InputStream is = ConfigLoader.getStream("/data/avstack/lax_cs_dl.json")) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("LAX"), SystemData.getAirline("DL"), dt, true, 0);
			assertNotNull(results);
			assertFalse(results.isEmpty());
			allFlights.addAll(results);
		}
		
		// Filter code shares, DL only
		int oldSize = allFlights.size();
		RawScheduleHelper.mergeCodeShares(allFlights, new CodeShareFilter(Set.of("DL")));
		assertTrue(allFlights.size() < oldSize);
		assertFalse(allFlights.isEmpty());
		assertEquals(0, allFlights.stream().filter(rse -> "AF25".equals(rse.getShortCode())).count());
		
		// Validate codeshares
		for (RawScheduleEntry rse : allFlights) {
			if ("DL".equals(rse.getAirline().getCode())) continue;
			Collection<String> cs = rse.getCodeShareOperators();
			assertTrue(cs.contains("DL"));
		}
		
		// Set line numbers
		RawScheduleHelper.calculateLineNumbers(allFlights);
	}
}
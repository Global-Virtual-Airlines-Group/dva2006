package org.deltava.dao.http;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.ScheduleTestCase;

import org.deltava.beans.schedule.*;
import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

public class TestGetAviationStackCS extends ScheduleTestCase {
	
	private Collection<RawScheduleEntry> load(String fileName, LocalDate dt)  throws DAOException, IOException {

		try (InputStream is = ConfigLoader.getStream("/data/avstack/" + fileName)) {
			GetAviationStack dao = new GetAviationStack();
			dao.setAircraft(_acTypes);
			dao.setStream(is);			
			PaginatedList<RawScheduleEntry> results = dao.get(SystemData.getAirport("LAX"), SystemData.getAirline("KL"), dt, true, 0); // airline and airport are ignored because setStream is called
			assertNotNull(results);
			assertFalse(results.isEmpty());
			validateFlights(results);
			log.info("Loaded {}", fileName);
			return results;
		}
	}

	public void testCodeShares() throws Exception {
	
		LocalDate dt = LocalDate.now().plusDays(14);
		List<RawScheduleEntry> allFlights = new ArrayList<RawScheduleEntry>();
		
		allFlights.addAll(load("lax_cs_dl.json", dt));
		allFlights.addAll(load("lax_cs_af.json", dt));
		
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
		
		allFlights.addAll(load("lax_cs_af.json", dt));
		allFlights.addAll(load("lax_cs_dl.json", dt));
		
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
	
	public void testCodeShareInfer() throws Exception {

		LocalDate dt = LocalDate.now().plusDays(14);
		List<RawScheduleEntry> allFlights = new ArrayList<RawScheduleEntry>();

		allFlights.addAll(load("csMerge_lax_dl_d1.json", dt));
		allFlights.addAll(load("csMerge_lax_dl_d2.json", dt));
		allFlights.addAll(load("csMerge_lax_af_d.json", dt));
		allFlights.addAll(load("csMerge_lax_kl_d.json", dt));
		
		// Merge code shares
		CodeShareFilter csf = new CodeShareFilter(Set.of("DL", "AF"));
		csf.setRemovePotential(true);
		
		int oldSize = allFlights.size();
		RawScheduleHelper.identifyCodeShares(allFlights, csf);
		assertTrue(allFlights.size() < oldSize);
		assertFalse(allFlights.isEmpty());
		
		// Validate codeshares
		for (RawScheduleEntry rse : allFlights) {
			if ("DL".equals(rse.getAirline().getCode()) || "AF".equals(rse.getAirline().getCode())) continue;
			Collection<String> cs = rse.getCodeShareOperators();
			assertTrue(cs.contains("DL") || cs.contains("AF"));
		}
		
		RawScheduleHelper.stripPotential(allFlights);

		// Set line numbers
		RawScheduleHelper.calculateLineNumbers(allFlights);
	}
}
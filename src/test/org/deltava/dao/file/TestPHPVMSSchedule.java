package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

public class TestPHPVMSSchedule extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true";

	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();
	
	private record ImportInfo(String FileName, String Airline) {
		ImportInfo(String filename) {
			this(filename, null);
		}
	}
	
	private final Collection<ImportInfo> FILES = List.of(new ImportInfo("dl.csv"), new ImportInfo("end.csv", "Endeavor Airlines"), new ImportInfo("sky.csv", "SkyWest Airlines"), new ImportInfo("af.csv"), new ImportInfo("klm.csv"));

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestPHPVMSSchedule.class);

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
	
	private Collection<RawScheduleEntry> load(ImportInfo inf) throws Exception {
		
		File f = new File("C:\\Temp\\phpvms", inf.FileName);
		assertTrue(f.exists());

		// Load the data
		log.info("Reading {}", inf.FileName);
		Collection<RawScheduleEntry> rawEntries = new ArrayList<RawScheduleEntry>();
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 131072)) {
			GetPHPVMSSchedule dao = new GetPHPVMSSchedule(is);
			dao.setAircraft(_acTypes);
			dao.setAirlines(SystemData.getAirlines());
			rawEntries.addAll(dao.process());
			ImportStatus st = dao.getStatus();
			st.getErrorMessages().forEach(log::info);
			if (!st.getInvalidAirports().isEmpty())
				log.warn("Invalid Airports - {}", st.getInvalidAirports());
			if (!st.getInvalidEquipment().isEmpty())
				log.warn("Invalid Aircraft - {}", st.getInvalidEquipment());
			
			assertFalse(rawEntries.isEmpty());
		}
		
		// Validate UTC usage and add operator
		for (RawScheduleEntry rse : rawEntries) {
			boolean useGMT = (rse.getAirportD().getTZ().hasDST() != rse.getAirportA().getTZ().hasDST());
			assertEquals(useGMT, rse.getIsUTC());
			if (!StringUtils.isEmpty(inf.Airline))
				rse.setRemarks(String.format("Operated by %S", inf.Airline));
		}

		// Group by departure airport
		log.info("Loaded {} raw schedule entries", Integer.valueOf(rawEntries.size()));
		Map<Airport, Collection<RawScheduleEntry>> rawMap = new HashMap<Airport, Collection<RawScheduleEntry>>();
		rawEntries.forEach(rse -> CollectionUtils.addMapCollection(rawMap, rse.getAirportD(), rse, ArrayList::new));

		// Purge based on departure time
		Comparator<RawScheduleEntry> cmp = ScheduleLegHelper.getDupeChecker(true); int dupeLegs = 0;
		for (Collection<RawScheduleEntry> entries : rawMap.values()) {
			Collection<RawScheduleEntry> apLegs = new TreeSet<RawScheduleEntry>(cmp);
			for (RawScheduleEntry rse : entries) {
				if (!apLegs.add(rse)) {
					log.debug("Removing {} from {}", rse, rse.getAirportD().getICAO());
					rawEntries.remove(rse);
					dupeLegs++;
				}
			}
		}
		
		// Calculate leg numbers
		log.info("Removed {} duplicate Flight Legs based on departure time", Integer.valueOf(dupeLegs));
		ScheduleLegHelper.calculateLegs(rawEntries);

		// Get today's flights - Map via flight code
		final LocalDate today = LocalDate.now();
		Map<String, List<ScheduleEntry>> fMap = new HashMap<String, List<ScheduleEntry>>();
		rawEntries.stream().map(rse -> rse.toToday(today)).filter(Objects::nonNull).forEach(se -> addEntry(fMap, se.getFlightCode(), se));
		assertNotNull(fMap);
		assertFalse(fMap.isEmpty());

		Supplier<IntStream> ss = () -> fMap.entrySet().stream().mapToInt(me -> me.getValue().size());
		long totalFlights = ss.get().summaryStatistics().getSum();
		long totalDupes = ss.get().filter(s -> (s > 1)).count();
		log.info("Processing {} flight codes for {}", Integer.valueOf(fMap.size()), today);
		log.info("Total Flights = {}, dupe Count = {}", Long.valueOf(totalFlights), Long.valueOf(totalDupes));

		ScheduleEntryComparator scmp = new ScheduleEntryComparator(ScheduleEntryComparator.DTIME);
		Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		for (List<ScheduleEntry> flights : fMap.values()) {
			if (flights.size() > 1) {
				Collections.sort(flights, scmp);
				for (int x = 1; x < flights.size(); x++)
					flights.get(x).setLeg(x + 1);
			}
			
			entries.addAll(flights);
		}
		
		// Make sure there are no dupes
		Collection<ScheduleEntry> uniqueCheck = new LinkedHashSet<ScheduleEntry>();
		for (ScheduleEntry se : entries) {
			boolean isUnique = uniqueCheck.add(se);
			if (!isUnique)
				log.warn("Duplicate Flight {}", se.getFlightCode());
		}
		
		assertEquals(entries.size(), uniqueCheck.size());
		return rawEntries;
	}

	public void testLoadRaw() throws Exception {
		
		// Load existing Entries
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		List<RawScheduleEntry> entries = rsdao.load(ScheduleSource.VASYS, null);
		
		// Load the files
		for (ImportInfo info : FILES) {
			Collection<RawScheduleEntry> rawEntries = load(info);
			Collection<Airline> airlines = rawEntries.stream().map(ScheduleEntry::getAirline).collect(Collectors.toSet());
			entries.removeIf(rse -> airlines.contains(rse.getAirline()));
			entries.addAll(rawEntries);
		}

		// Set line number
		for (int x = 0; x < entries.size(); x++) {
			RawScheduleEntry rse = entries.get(x);
			rse.setLineNumber(x + 1);
		}

		// Purge and write
		SetSchedule rwdao = new SetSchedule(_c);
		rwdao.purgeRaw(ScheduleSource.VASYS);
		for (RawScheduleEntry rse : entries)
			rwdao.writeRaw(rse, false);
		
		//_c.commit();
		log.info("Wrote {} raw schedule entries", Integer.valueOf(entries.size()));
		
		// Export to JSON file
		JSONScheduleFormatter fmt = new JSONScheduleFormatter();
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("C:\\Temp", "phpvms.json"))))) {
			pw.print(fmt.getHeader());
			for (Iterator<RawScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				RawScheduleEntry rse = i.next();
				pw.print(fmt.format(rse));
				if (i.hasNext()) {
					pw.print(fmt.getSeparator());
					pw.println();
				}
			}
			
			pw.println(fmt.getFooter());
		}
	}
	
	private static <K, V> void addEntry(Map<K, List<V>> m, K key, V value) {
		List<V> c = m.get(key);
		if (c == null) {
			c = new ArrayList<V>();
			m.put(key, c);
		}
		
		c.add(value);
	}
}
package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.dao.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

public class TestPHPVMSSchedule extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true";

	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestPHPVMSSchedule.class);

		CacheManager.init("TEST");
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
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
		_acTypes.addAll(acdao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	public void testLoadRaw() throws Exception {
		
		File f = new File("C:\\Temp\\vasystems.csv");
		assertTrue(f.exists());
		
		Collection<RawScheduleEntry> rawEntries = new ArrayList<RawScheduleEntry>();
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 131072)) {
			GetPHPVMSSchedule dao = new GetPHPVMSSchedule(is);
			dao.setAircraft(_acTypes);
			dao.setAirlines(SystemData.getAirlines());
			rawEntries.addAll(dao.process());
			assertFalse(rawEntries.isEmpty());
		}
		
		SetSchedule rwdao = new SetSchedule(_c);
		rwdao.purgeRaw(ScheduleSource.VASYS);
		for (RawScheduleEntry rse : rawEntries)
			rwdao.writeRaw(rse, false);
		
		_c.commit();
		log.info("Wrote {} raw schedule entries", Integer.valueOf(rawEntries.size()));
		
		// Get from the database
		final LocalDate today = LocalDate.now();
		GetRawSchedule rawdao = new GetRawSchedule(_c);
		Collection<RawScheduleEntry> todaysRaw = rawdao.load(ScheduleSource.VASYS, today);
		assertNotNull(todaysRaw);
		assertFalse(todaysRaw.isEmpty());
		
		// Get today's flights - Map via flight code
		Map<String, List<ScheduleEntry>> fMap = new HashMap<String, List<ScheduleEntry>>();
		rawEntries.stream().map(rse -> rse.toToday(today)).filter(Objects::nonNull).forEach(se -> addEntry(fMap, se.getFlightCode(), se));
		assertNotNull(fMap);
		assertFalse(fMap.isEmpty());
		
		Supplier<IntStream> ss = () -> fMap.entrySet().stream().mapToInt(me -> me.getValue().size());
		long totalFlights = ss.get().summaryStatistics().getSum();
		long totalDupes = ss.get().filter(s -> (s > 1)).count();
		assertEquals(totalFlights, todaysRaw.size());
		
		log.info("Processing {} flight codes for {}", Integer.valueOf(fMap.size()), today);
		log.info("Total Flights = {}, dupe Count = {}", Long.valueOf(totalFlights), Long.valueOf(totalDupes));
		
		ScheduleEntryComparator cmp = new ScheduleEntryComparator(ScheduleEntryComparator.DTIME);
		Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		for (List<ScheduleEntry> flights : fMap.values()) {
			if (flights.size() > 1) {
				Collections.sort(flights, cmp);
				for (int x = 1; x < flights.size(); x++)
					flights.get(x).setLeg(x + 1);
			}
			
			entries.addAll(flights);
		}
		
		// Make sure there are no dupes
		Collection<ScheduleEntry> uniqueCheck = new LinkedHashSet<ScheduleEntry>(entries);
		assertNotNull(uniqueCheck);
		assertEquals(entries.size(), uniqueCheck.size());
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
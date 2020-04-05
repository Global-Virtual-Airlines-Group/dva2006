package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class TestDeltaScheduleLoad extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";

	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(TestDeltaScheduleLoad.class);

		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
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
		_acTypes.addAll(acdao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}
	
	@SuppressWarnings("static-method")
	public void testConvertPDF() throws Exception {
		
		File f = new File("C:\\Temp\\flight_schedules.pdf");
		assertTrue(f.exists());
		
		File txtF = new File("C:\\Temp\\flight_schedules.txt");
		if (f.exists())
			return;
		
		try (InputStream is = new FileInputStream(f)) {
			GetPDFText prdao = new GetPDFText(is);
			String txt = prdao.getText();
			try (OutputStream os = new BufferedOutputStream(new FileOutputStream(txtF), 131072); PrintWriter pw = new PrintWriter(os)) {
				pw.write(txt);
			}
		}
	}

	public void testLoadRaw() throws Exception {
		
		File f = new File("C:\\Temp\\delta_schedule.txt");
		assertTrue(f.exists());
		
		Collection<RawScheduleEntry> rawEntries = new ArrayList<RawScheduleEntry>();
		try (InputStream is = new FileInputStream(f)) {
			GetDeltaSchedule dao = new GetDeltaSchedule(is);
			dao.setAircraft(_acTypes);
			dao.setAirlines(SystemData.getAirlines().values());
			rawEntries.addAll(dao.process());
			assertFalse(rawEntries.isEmpty());
		}
		
		assertFalse(rawEntries.isEmpty());
		assertFalse(true);
		
		SetSchedule rwdao = new SetSchedule(_c);
		rwdao.purgeRaw(ScheduleSource.DELTA);
		for (RawScheduleEntry rse : rawEntries)
			rwdao.writeRaw(rse);
		
		_c.commit();
		log.info("Wrote " + rawEntries.size() + " raw schedule entries");
		
		// Get from the database
		final LocalDate today = LocalDate.now();
		GetRawSchedule rawdao = new GetRawSchedule(_c);
		Collection<RawScheduleEntry> todaysRaw = rawdao.load(ScheduleSource.DELTA, today);
		assertNotNull(todaysRaw);
		
		// Get today's flights - Map via short code
		Map<String, List<ScheduleEntry>> fMap = new HashMap<String, List<ScheduleEntry>>();
		rawEntries.stream().map(rse -> rse.toToday(today)).filter(Objects::nonNull).forEach(se -> addEntry(fMap, se.getShortCode(), se));
		assertNotNull(fMap);
		assertFalse(fMap.isEmpty());
		
		Supplier<IntStream> ss = () -> fMap.entrySet().stream().mapToInt(me -> me.getValue().size());
		long totalFlights = ss.get().summaryStatistics().getSum();
		long totalDupes = ss.get().filter(s -> (s > 1)).count();
		assertEquals(totalFlights, todaysRaw.size());
		
		log.info("Processing " + fMap.size() + " flight codes for " + today);
		log.info("Total Flights = " + totalFlights + ", dupe Count = " + totalDupes);
		
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
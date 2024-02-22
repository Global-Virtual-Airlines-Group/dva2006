package org.deltava;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.Flight;
import org.deltava.beans.schedule.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadCSVSchedule extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?rewriteBatchedStatements=true&useSSL=false&allowPublicKeyRetrieval=true";
	
	private Logger log;
	
	private Connection _c;
	private final Collection<Aircraft> _allAC = new ArrayList<Aircraft>();
	
	private final ScheduleSource SRC = ScheduleSource.DELTA;
	private static final boolean COMMIT = true; 
	
	private static final Collection<String> AIRLINES = Set.of("DAL","WJA","AMX","KL","AF");
	//private static final Collection<String> AIRLINES = Set.of("DAL");
	
	private static final Map<String, String> DVC_AIRLINES = Map.of("EDV", "Endeavor Air", "RPA", "Republic Airways", "SKW", "SkyWest Airlines");

	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("M[M]/d[d]/yyyy").parseLenient().toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").toFormatter();
	
	private static class ScheduleEntryComparator implements Comparator<RawScheduleEntry> {
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			int tmpResult = rse1.getFlightCode().compareTo(rse2.getFlightCode());
			if (tmpResult != 0)
				tmpResult = rse1.createKey().compareTo(rse2.createKey());
			
			return (tmpResult == 0) ? rse1.getEndDate().compareTo(rse2.getEndDate()) : tmpResult;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(LoadCSVSchedule.class);
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		
		// Load the airports/time zones
		SystemData.init();
		SystemData.add("airline.code", "DVA");
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		// Load aircraft
		GetAircraft acdao = new GetAircraft(_c);
		_allAC.addAll(acdao.getAircraftTypes());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.rollback();
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}
	
	private RawScheduleEntry parse(int ln, CSVTokens csv, String airlineCode) {
		Flight f = FlightCodeParser.parse(csv.get(3), airlineCode);
		if (f == null) {
			log.warn("Unparseable flight {} at Line {}", csv.get(3), Integer.valueOf(ln));
			return null;
		}
		
		RawScheduleEntry rse = new RawScheduleEntry(f);
		rse.setAirportD(SystemData.getAirport(csv.get(0)));
		rse.setAirportA(SystemData.getAirport(csv.get(1)));
		if (rse.getAirportD() == null) {
			log.warn("Unknown Airport {} at Line {}", csv.get(0), Integer.valueOf(ln));
			return null;
		} else if (rse.getAirportA() == null) {
			log.warn("Unknown Airport {} at Line {}", csv.get(1), Integer.valueOf(ln));
			return null;
		}
		
		rse.setStartDate(LocalDate.parse(csv.get(7), _df));
		rse.addDayOfWeek(rse.getStartDate().getDayOfWeek());
		rse.setEndDate(rse.getStartDate());
		rse.setTimeD(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(5), _tf)));
		rse.setTimeA(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(6), _tf)));
		rse.setSource(SRC);
		rse.setForceInclude(true);
		
		String ac = csv.get(4).toUpperCase();
		Aircraft a = _allAC.stream().filter(ap -> ap.getIATA().contains(ac)).findAny().orElse(null);
		if (a == null) {
			log.warn("Unknown aircraft code {} at Line {}", ac, Integer.valueOf(ln));
			return null;
		}
		
		rse.setEquipmentType(a.getName());
		return rse;
	}
	
	private RawScheduleEntry parseDAL(int ln, CSVTokens csv) {
		RawScheduleEntry rse = new RawScheduleEntry(FlightCodeParser.parse(csv.get(4), "DAL"));
		rse.setAirportD(SystemData.getAirport(csv.get(0)));
		rse.setAirportA(SystemData.getAirport(csv.get(1)));
		if (rse.getAirportD() == null) {
			log.warn("Unknown Airport {} at Line {}", csv.get(0), Integer.valueOf(ln));
			return null;
		} else if (rse.getAirportA() == null) {
			log.warn("Unknown Airport {} at Line {}", csv.get(1), Integer.valueOf(ln));
			return null;
		}
		
		// Check for connection
		if (DVC_AIRLINES.containsKey(csv.get(2)))
			rse.setRemarks("Operated by " + DVC_AIRLINES.get(csv.get(2)));
		
		rse.setStartDate(LocalDate.parse(csv.get(9), _df));
		rse.addDayOfWeek(rse.getStartDate().getDayOfWeek());
		rse.setEndDate(rse.getStartDate());
		rse.setTimeD(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(7), _tf)));
		rse.setTimeA(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(8), _tf)));
		rse.setSource(SRC);
		rse.setForceInclude(true);
		
		String ac = csv.get(5).toUpperCase();
		Aircraft a = _allAC.stream().filter(ap -> ap.getIATA().contains(ac)).findAny().orElse(null);
		if (a == null) {
			log.warn("Unknown aircraft code {} at Line {}", ac, Integer.valueOf(ln));
			return null;
		}
		
		rse.setEquipmentType(a.getName());
		return rse;
	}
	
	private List<RawScheduleEntry> dedupe(Map<String, Collection<RawScheduleEntry>> buckets) {
		
		int total = 0;
		ScheduleEntryComparator cmp = new ScheduleEntryComparator();
		List<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		for (Map.Entry<String, Collection<RawScheduleEntry>> me : buckets.entrySet()) {
			LinkedList<RawScheduleEntry> b2 = new LinkedList<RawScheduleEntry>(me.getValue());
			b2.sort(cmp); total += b2.size();
			RawScheduleEntry rse = b2.removeFirst(); RawScheduleEntry rse2 = b2.peek();
			entries.add(rse);
			while (rse2 != null) {
				b2.removeFirst(); // remove rse2
				if (rse2.getTimeD().toLocalTime().equals(rse.getTimeD().toLocalTime()) && rse2.getTimeA().toLocalTime().equals(rse.getTimeA().toLocalTime())) {
					rse.setEndDate(rse2.getEndDate());
					rse2.getDays().forEach(rse::addDayOfWeek);
				} else {
					rse = rse2;
					entries.add(rse);				}
					
				rse2 = b2.peek();
			}
		}
		
		log.info("Deduped {} entries into {}", Integer.valueOf(total), Integer.valueOf(entries.size()));
		return entries;
	}
	
	public void testWestjet() throws Exception {
		
		File f = new File("C:\\Temp", "WJA_0224.csv");
		assertTrue(f.exists());
		assertTrue(AIRLINES.contains("WJA"));
		
		final int baseLine = 3_000_000;

		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "WJA");
				if (rse == null) continue;
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		List<RawScheduleEntry> entries = dedupe(buckets);
		log.info("Loaded {} schedule entries", Integer.valueOf(entries.size()));
		SetSchedule swdao = new SetSchedule(_c);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		if (COMMIT)
			_c.commit();
	}

	public void testDelta() throws Exception {
		
		File f = new File("C:\\Temp", "DAL_0224.csv");
		assertTrue(f.exists());
		assertTrue(AIRLINES.contains("DAL"));
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parseDAL(lr.getLineNumber(), csv);
				if (rse == null) continue;
				rse.setLineNumber(lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		List<RawScheduleEntry> entries = dedupe(buckets);
		entries.stream().filter(rse -> "B737-900".equals(rse.getEquipmentType())).forEach(rse -> rse.setEquipmentType("B737-900ER"));
		log.info("Loaded {} schedule entries", Integer.valueOf(entries.size()));
		
		SetSchedule swdao = new SetSchedule(_c);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		if (COMMIT)
			_c.commit();
	}
	
	public void testAeromexico() throws Exception {

		File f = new File("C:\\Temp", "AMX_0224.csv");
		assertTrue(f.exists());
		assertTrue(AIRLINES.contains("AMX"));
		
		// Get source info
		final int baseLine = 2_000_000;
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "AM");
				if (rse == null) continue;	
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		List<RawScheduleEntry> entries = dedupe(buckets);
		log.info("Loaded {} schedule entries", Integer.valueOf(entries.size()));
		SetSchedule swdao = new SetSchedule(_c);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		if (COMMIT)
			_c.commit();
	}
	
	public void testKLM() throws Exception {

		File f = new File("C:\\Temp", "KLM_0224.csv");
		assertTrue(f.exists());
		assertTrue(AIRLINES.contains("KL"));
		
		// Get source info
		final int baseLine = 4_000_000;
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "KL");
				if (rse == null) continue;	
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		List<RawScheduleEntry> entries = dedupe(buckets);
		log.info("Loaded {} schedule entries", Integer.valueOf(entries.size()));
		SetSchedule swdao = new SetSchedule(_c);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);

		if (COMMIT)
			_c.commit();
	}
	
	public void testAirFrance() throws Exception {

		File f = new File("C:\\Temp", "AFR_0224.csv");
		assertTrue(f.exists());
		assertTrue(AIRLINES.contains("AF"));
		
		// Get source info
		final int baseLine = 5_000_000;
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "KL");
				if (rse == null) continue;	
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		List<RawScheduleEntry> entries = dedupe(buckets);
		log.info("Loaded {} schedule entries", Integer.valueOf(entries.size()));
		SetSchedule swdao = new SetSchedule(_c);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);

		if (COMMIT)
			_c.commit();
	}
}
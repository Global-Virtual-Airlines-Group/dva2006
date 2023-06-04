package org.deltava;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadCSVSchedule extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?rewriteBatchedStatements=true&useSSL=false";
	
	private Logger log;
	
	private Connection _c;
	private final Collection<Aircraft> _allAC = new ArrayList<Aircraft>();
	
	private final ScheduleSource SRC = ScheduleSource.DELTA; 

	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("MM/dd/yyyy").toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();
	
	private class ScheduleEntryComparator implements Comparator<RawScheduleEntry> {
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
		_allAC.addAll(acdao.getAll());
		_c.setAutoCommit(false);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.rollback();
		_c.close();
		super.tearDown();
	}
	
	private RawScheduleEntry parse(int ln, CSVTokens csv, String airlineCode) {
		RawScheduleEntry rse = new RawScheduleEntry(FlightCodeParser.parse(csv.get(6), airlineCode));
		rse.setAirportD(SystemData.getAirport(csv.get(1)));
		rse.setAirportA(SystemData.getAirport(csv.get(3)));
		if (rse.getAirportD() == null) {
			log.warn("Unknown Airport " + csv.get(1) + " at Line " + ln);
			return null;
		} else if (rse.getAirportA() == null) {
			log.warn("Unknown Airport " + csv.get(3) + " at Line " + ln);
			return null;
		}
		
		rse.setStartDate(LocalDate.parse(csv.get(14), _df));
		rse.addDayOfWeek(rse.getStartDate().getDayOfWeek());
		rse.setEndDate(rse.getStartDate());
		rse.setTimeD(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(9), _tf)));
		rse.setTimeA(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(10), _tf)));
		rse.setSource(SRC);
		rse.setForceInclude(true);
		
		String ac = csv.get(11).toUpperCase();
		Aircraft a = _allAC.stream().filter(ap -> ap.getIATA().contains(ac)).findAny().orElse(null);
		if (a == null) {
			log.warn("Unknown aircraft code - " + ac);
			return null;
		}
		
		rse.setEquipmentType(a.getName());
		return rse;
	}
	
	public void testWestjet() throws Exception {
		
		File f = new File("C:\\Temp", "WJA_June-2023.csv");
		assertTrue(f.exists());

		// Get source info
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		ScheduleSourceInfo srcInfo = rsdao.getSources(false, "dva").stream().filter(ssi -> ssi.getSource() == SRC).findFirst().orElse(null);
		int baseLine = (srcInfo == null) ? 0 : srcInfo.getMaxLineNumber();

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
		
		ScheduleEntryComparator cmp = new ScheduleEntryComparator();
		List<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		for (Map.Entry<String, Collection<RawScheduleEntry>> me : buckets.entrySet()) {
			LinkedList<RawScheduleEntry> b2 = new LinkedList<RawScheduleEntry>(me.getValue());
			b2.sort(cmp);
			RawScheduleEntry rse = b2.removeFirst(); RawScheduleEntry rse2 = b2.peek();
			entries.add(rse);
			while (rse2 != null) {
				b2.removeFirst(); // remove rse2
				if (rse2.getTimeD().toLocalTime().equals(rse.getTimeD().toLocalTime()) && rse2.getTimeA().toLocalTime().equals(rse.getTimeA().toLocalTime())) {
					rse.setEndDate(rse2.getEndDate());
					rse2.getDays().forEach(rse::addDayOfWeek);
				} else {
					rse = rse2;
					entries.add(rse);
				}
					
				rse2 = b2.peek();
			}
		}
		
		SetSchedule swdao = new SetSchedule(_c);
		//swdao.purge(SRC);
		for (RawScheduleEntry rse : entries)
				swdao.writeRaw(rse, false);
		
		_c.commit();
	}

	public void testDelta() throws Exception {
		
		File f = new File("C:\\Temp", "DAL_June-2023.csv");
		assertTrue(f.exists());
		
		// Get source info
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		ScheduleSourceInfo srcInfo = rsdao.getSources(false, "dva").stream().filter(ssi -> ssi.getSource() == SRC).findFirst().orElse(null);
		int baseLine = (srcInfo == null) ? 0 : srcInfo.getMaxLineNumber();
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "DAL");
				if (rse == null) continue;
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		ScheduleEntryComparator cmp = new ScheduleEntryComparator();
		List<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		for (Map.Entry<String, Collection<RawScheduleEntry>> me : buckets.entrySet()) {
			LinkedList<RawScheduleEntry> b2 = new LinkedList<RawScheduleEntry>(me.getValue());
			b2.sort(cmp);
			RawScheduleEntry rse = b2.removeFirst(); RawScheduleEntry rse2 = b2.peek();
			entries.add(rse);
			while (rse2 != null) {
				b2.removeFirst(); // remove rse2
				if (rse2.getTimeD().toLocalTime().equals(rse.getTimeD().toLocalTime()) && rse2.getTimeA().toLocalTime().equals(rse.getTimeA().toLocalTime())) {
					rse.setEndDate(rse2.getEndDate());
					rse2.getDays().forEach(rse::addDayOfWeek);
				} else {
					rse = rse2;
					entries.add(rse);
				}
					
				rse2 = b2.peek();
			}
		}
		
		entries.stream().filter(rse -> "B737-900".equals(rse.getEquipmentType())).forEach(rse -> rse.setEquipmentType("B737-900ER"));
		
		SetSchedule swdao = new SetSchedule(_c);
		//swdao.purge(SRC);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		_c.commit();
	}
	
	public void testAeromexico() throws Exception {

		File f = new File("C:\\Temp", "AMX_June-2023.csv");
		assertTrue(f.exists());
		
		// Get source info
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		ScheduleSourceInfo srcInfo = rsdao.getSources(false, "dva").stream().filter(ssi -> ssi.getSource() == SRC).findFirst().orElse(null);
		int baseLine = (srcInfo == null) ? 0 : srcInfo.getMaxLineNumber();
		
		// Put entries into buckets
		Map<String, Collection<RawScheduleEntry>> buckets = new HashMap<String, Collection<RawScheduleEntry>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = parse(lr.getLineNumber(), csv, "AMX");
				if (rse == null) continue;
				rse.setLineNumber(baseLine + lr.getLineNumber());
				CollectionUtils.addMapCollection(buckets, String.format("%s-%s",rse.createKey(), rse.getFlightCode()), rse, ArrayList::new);
			}
		}
		
		ScheduleEntryComparator cmp = new ScheduleEntryComparator();
		List<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		for (Map.Entry<String, Collection<RawScheduleEntry>> me : buckets.entrySet()) {
			LinkedList<RawScheduleEntry> b2 = new LinkedList<RawScheduleEntry>(me.getValue());
			b2.sort(cmp);
			RawScheduleEntry rse = b2.removeFirst(); RawScheduleEntry rse2 = b2.peek();
			entries.add(rse);
			while (rse2 != null) {
				b2.removeFirst(); // remove rse2
				if (rse2.getTimeD().toLocalTime().equals(rse.getTimeD().toLocalTime()) && rse2.getTimeA().toLocalTime().equals(rse.getTimeA().toLocalTime())) {
					rse.setEndDate(rse2.getEndDate());
					rse2.getDays().forEach(rse::addDayOfWeek);
				} else {
					rse = rse2;
					entries.add(rse);
				}
					
				rse2 = b2.peek();
			}
		}
		
		SetSchedule swdao = new SetSchedule(_c);
		//swdao.purge(SRC);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		_c.commit();
	}
}
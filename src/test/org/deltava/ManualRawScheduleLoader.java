package org.deltava;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class ManualRawScheduleLoader extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/afv?useSSL=false";
	
	private static Logger log;

	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(ManualRawScheduleLoader.class);
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
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testCopyManualEntries() throws Exception {
		assertFalse(true);
		
		// Load manual entries
		GetSchedule sdao = new GetSchedule(_c);
		List<ScheduleEntry> allEntries = sdao.export().stream().filter(se -> ((se.getSource() == ScheduleSource.MANUAL) || (se.getSource() == null))).collect(Collectors.toList());
		assertNotNull(allEntries);
		
		// Convert to raw Entries
		List<RawScheduleEntry> rawEntries = allEntries.parallelStream().map(ManualRawScheduleLoader::toRaw).collect(Collectors.toList());
		assertEquals(allEntries.size(), rawEntries.size());
		
		// Write to the database
		SetSchedule swdao = new SetSchedule(_c); int ln = 1;
		for (RawScheduleEntry rse : rawEntries) {
			rse.setLineNumber(ln); ln++;
			swdao.writeRaw(rse);
		}
		
		// Commit
		_c.commit();
	}
	
	public void testCopySpecificAirlines() throws Exception {
		List<String> airlineCodes = List.of("ALP", "VD", "MU", "CZ");
		
		// Get max src line
		int maxLine = 0;
		try (PreparedStatement ps = _c.prepareStatement("SELECT MAX(SRCLINE) FROM RAW_SCHEDULE WHERE (SRC=?)")) {
			ps.setInt(1, ScheduleSource.LEGACY.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				maxLine = rs.next() ? rs.getInt(1) : 0;
			}
		}
		
		// Load what we already have
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		List<RawScheduleEntry> rawEntries = rsdao.load(ScheduleSource.LEGACY, LocalDate.now()).stream().filter(se -> airlineCodes.contains(se.getAirline().getCode())).collect(Collectors.toList());
		log.info("Loaded " + rawEntries.size() + " raw Schdule Entries");
		
		// Load the new stuff
		GetScheduleSearch sdao = new GetScheduleSearch(_c);
		sdao.setSources(rsdao.getSources(false, "afv"));
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		for (String aCode : airlineCodes) {
			ScheduleSearchCriteria ssc = new ScheduleSearchCriteria(SystemData.getAirline(aCode), 0, 0);
			ssc.setDBName("afv");
			Collection<ScheduleEntry> entries = sdao.search(ssc);
			log.info("Loaded " + entries.size() + " Schdule Entries for " + aCode);
			for (ScheduleEntry se : entries) {
				boolean isFound = rawEntries.stream().filter(rse -> rse.equals(se)).findAny().isPresent();
				if (!isFound) {
					log.info("Missing " + se.getFlightCode());
					results.add(toRaw(se));
				}
			}
		}
		
		// Write to the database
		SetSchedule swdao = new SetSchedule(_c); int ln = maxLine + 1;
		for (RawScheduleEntry rse : results) {
			rse.setSource(ScheduleSource.LEGACY);
			rse.setLineNumber(ln); ln++;
			swdao.writeRaw(rse);
		}
		
		// Commit
		_c.commit();
	}
	
	private static RawScheduleEntry toRaw(ScheduleEntry se) {
		RawScheduleEntry rse = new RawScheduleEntry(se);
		rse.setStartDate(LocalDate.of(2005, 1, 1));
		rse.setEndDate(LocalDate.of(2029, 12, 31));
		rse.setAirportD(se.getAirportD());
		rse.setAirportA(se.getAirportA());
		rse.setSource((se.getSource() == null) ? ScheduleSource.MANUAL : se.getSource());
		rse.setEquipmentType(se.getEquipmentType());
		rse.setTimeD(se.getTimeD().toLocalDateTime());
		rse.setTimeA(se.getTimeA().toLocalDateTime());
		rse.setAcademy(se.getAcademy());
		rse.setHistoric(se.getHistoric() || se.getAirline().getHistoric());
		for (DayOfWeek d : DayOfWeek.values())
			rse.addDayOfWeek(d);
		
		return rse;
	}
}
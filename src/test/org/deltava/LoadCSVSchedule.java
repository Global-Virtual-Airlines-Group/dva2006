package org.deltava;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadCSVSchedule extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?rewriteBatchedStatements=true&useSSL=false";
	
	private Logger log;
	
	private Connection _c;
	
	private final ScheduleSource SRC = ScheduleSource.LEGACY; 

	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("MM/dd/yyyy").toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(LoadCSVSchedule.class);
		
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
	}

	@Override
	protected void tearDown() throws Exception {
		_c.rollback();
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testWestJet() throws Exception {
		
		File f = new File("C:\\Temp", "wja_schedule_march.csv");
		assertTrue(f.exists());
		
		// Get source info
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		ScheduleSourceInfo srcInfo = rsdao.getSources(false, "dva").stream().filter(ssi -> ssi.getSource() == SRC).findFirst().orElse(null);
		int baseLine = (srcInfo == null) ? 0 : srcInfo.getMaxLineNumber();
		
		// Load aircraft
		GetAircraft acdao = new GetAircraft(_c);
		Collection<Aircraft> allAC = acdao.getAll();
		_c.setAutoCommit(false);
		
		Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 131072)) {
			String data = lr.readLine(); // skip first line
			while (data != null) {
				data = lr.readLine();
				if (data == null) break;
				CSVTokens csv = StringUtils.parseCSV(data);
				RawScheduleEntry rse = new RawScheduleEntry(FlightCodeParser.parse(csv.get(2), "WJA"));
				rse.setAirportD(SystemData.getAirport(csv.get(0)));
				rse.setAirportA(SystemData.getAirport(csv.get(1)));
				if (rse.getAirportD() == null) {
					log.warn("Unknown Airport " + csv.get(0) + " at Line " + lr.getLineNumber());
					continue;
				} else if (rse.getAirportA() == null) {
					log.warn("Unknown Airport " + csv.get(1) + " at Line " + lr.getLineNumber());
					continue;
				}
				
				rse.setStartDate(LocalDate.parse(csv.get(6), _df));
				rse.setEndDate(rse.getStartDate());
				rse.setTimeD(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(3), _tf)));
				rse.setTimeA(LocalDateTime.of(rse.getStartDate(), LocalTime.parse(csv.get(4), _tf)));
				rse.setSource(SRC);
				rse.setLineNumber(baseLine + lr.getLineNumber());
				rse.setForceInclude(true);
				
				String ac = csv.get(5).toUpperCase();
				Aircraft a = allAC.stream().filter(ap -> ap.getIATA().contains(ac)).findAny().orElse(null);
				if (a == null) {
					log.warn("Unknown aircraft code - " + ac);
					continue;
				}
				
				rse.setEquipmentType(a.getName());
				
				List<RawScheduleEntry> rsMatches = entries.stream().filter(re -> re.getFlightCode().equals(rse.getFlightCode())).collect(Collectors.toList());
				if (rsMatches.size() > 0) {
					RawScheduleEntry rse2 = rsMatches.get(rsMatches.size() - 1);
					if (rse2.getTimeD().toLocalTime().equals(rse.getTimeD().toLocalTime()) && rse2.getTimeA().toLocalTime().equals(rse.getTimeA().toLocalTime()))
						rse2.setEndDate(rse.getEndDate());
					else
						entries.add(rse);
				} else 
					entries.add(rse);
			}
		}
		
		SetSchedule swdao = new SetSchedule(_c);
		swdao.purge(ScheduleSource.LEGACY);
		for (RawScheduleEntry rse : entries)
			swdao.writeRaw(rse, false);
		
		_c.commit();
	}
}
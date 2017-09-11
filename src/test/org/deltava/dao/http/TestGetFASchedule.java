package org.deltava.dao.http;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class TestGetFASchedule extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";
	
	private static Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestGetFASchedule.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
			assertNotNull(c);
		
			// Initialize System Data
			SystemData.init();
			
			// Load Time Zones
			GetTimeZone dao = new GetTimeZone(c);
			log.info("Loaded " + dao.initAll() + " Time Zones");
			
			// Load country codes
			log.info("Loading Country codes");
			GetCountry cdao = new GetCountry(c);
			log.info("Loaded " + cdao.initAll() + " Country codes");

			// Load Database information
			log.info("Loading Cross-Application data");
			GetUserData uddao = new GetUserData(c);
			SystemData.add("apps", uddao.getAirlines(true));

			// Load active airlines
			log.info("Loading Airline Codes");
			GetAirline aldao = new GetAirline(c);
			SystemData.add("airlines", aldao.getAll());

			// Load airports
			log.info("Loading Airports");
			GetAirport apdao = new GetAirport(c);
			SystemData.add("airports", apdao.getAll());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	private static Collection<RawScheduleEntry> load(String fileName) throws IOException {
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(fileName))) {
			String data = lr.readLine();
			while (data != null) {
				List<String> parts = StringUtils.split(data, ",");
				RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(parts.get(0)), StringUtils.parse(parts.get(1), 0));
				se.setAirportD(SystemData.getAirport(parts.get(2)));
				se.setAirportA(SystemData.getAirport(parts.get(3)));
				se.setEquipmentType(parts.get(4));
				ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(parts.get(5))), se.getAirportD().getTZ().getZone());
				se.setTimeD(zdt.toLocalDateTime());
				ZonedDateTime zat = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(parts.get(6))), se.getAirportA().getTZ().getZone());
				se.setTimeA(zat.toLocalDateTime());
				se.setCodeShare(parts.get(7));
				se.setCapacity(StringUtils.parse(parts.get(8), 0), StringUtils.parse(parts.get(9), 0), StringUtils.parse(parts.get(10), 0));
				results.add(se);
				data = lr.readLine();
			}
		} catch (FileNotFoundException fne) {
			// empty
		}
		
		return results;
	}
	
	private static void save(Collection<RawScheduleEntry> data, String fileName) throws IOException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
			for (RawScheduleEntry se : data) {
				pw.print(se.getAirline().getCode());
				pw.print(',');
				pw.print(se.getFlightNumber());
				pw.print(',');
				pw.print(se.getAirportD().getIATA());
				pw.print(',');
				pw.print(se.getAirportA().getIATA());
				pw.print(',');
				pw.print(se.getEquipmentType());
				pw.print(',');
				pw.print(se.getTimeD().toEpochSecond());
				pw.print(',');
				pw.print(se.getTimeA().toEpochSecond());
				pw.print(',');
				if (!StringUtils.isEmpty(se.getCodeShare()))
					pw.print(se.getCodeShare());
				
				pw.print(',');
				pw.print(se.getFirst());
				pw.print(',');
				pw.print(se.getBusiness());
				pw.print(',');
				pw.println(se.getEconomy());
			}
		}
	}

	@SuppressWarnings("static-method")
	public void testLoadSchedule() throws Exception {
		
		Airline a = SystemData.getAirline("AF");
		String csvFile = "C:\\Temp\\flightAware_" + a.getCode() + ".csv";
		log.info("Loading previous flights");
		Collection<RawScheduleEntry> flights = load(csvFile); int ofs = flights.size();
		log.info("Pre-Loaded " + ofs + " schedule entries");
		
		GetFASchedule fadao = new GetFASchedule();
		fadao.setReadTimeout(10000);
		fadao.setUser(SystemData.get("schedule.flightaware.flightXML.user"));
		fadao.setPassword(SystemData.get("schedule.flightaware.flightXML.v3"));
		
		Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS);

		Collection<RawScheduleEntry> entries = fadao.getSchedule(a, ofs, startDate, 7, false); 
		assertNotNull(entries);
		while (!entries.isEmpty()) {
			ofs += entries.size();
			flights.addAll(entries);
			save(entries, csvFile);
			log.info("Loaded " + flights.size() + " schedule entries");
			fadao.reset();
			entries = fadao.getSchedule(a, ofs, startDate, 7, false);
		}
		
		flights.addAll(entries);
		if (!entries.isEmpty())
			save(entries, csvFile);
	}
}
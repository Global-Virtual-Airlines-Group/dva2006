package org.deltava;

import java.sql.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadAVStackSchedule extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true";
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";
	
	private static final String AV_API_KEY = "fd7219da540b2130353b6b075ad7178f";
	
	private static final int SLEEP_INTERVAL = 62_500;
	private static final Object KEY = "$KEY";
	
	private Logger log;
	
	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();
	
	private File _c1 = new File(System.getProperty("java.io.tmpdir"), "AirportCache.data");
	private File _c2 = new File(System.getProperty("java.io.tmpdir"), "ScheduleEntries.data");
	
	private static final String AIRLINE_CODE = "WS";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(LoadAVStackSchedule.class);

		CacheManager.init("TEST");
		SystemData.init();

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD);
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
	
	private Collection<RawScheduleEntry> loadFlights(Hub h, LocalDate ld) throws DAOException {
		
		// Get the API DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(AV_API_KEY);
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(19500);
		avdao.setCompression(Compression.GZIP, Compression.DEFLATE);
		avdao.setAircraft(_acTypes);

		// Load the departure flights
		int ofs = 0;
		Collection<RawScheduleEntry> apEntries = new ArrayList<RawScheduleEntry>();
		log.info("Loading Departures for {} ({}) (ofs={})", h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
		PaginatedList<RawScheduleEntry> entries = avdao.get(h.getAirport(), h.getAirline(), ld, true);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), h.getAirport().getIATA());
		apEntries.addAll(entries);
		ThreadUtils.sleep(SLEEP_INTERVAL);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading Departures for{} ({}) (ofs={{})", h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(h.getAirport(), h.getAirline(), ld, true, ofs);
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}
		
		// Load the arrival flights
		ofs = 0;
		log.info("Loading Arrivals for {} ({}) (ofs={})", h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
		entries = avdao.get(h.getAirport(), h.getAirline(), ld, false);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), h.getAirport().getIATA());
		apEntries.addAll(entries);
		ThreadUtils.sleep(SLEEP_INTERVAL);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading Arrivals for{} ({}) (ofs={{})", h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(h.getAirport(), h.getAirline(), ld, false, ofs);
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}
		
		return apEntries;
	}
	
	public void testLoadSchedule() throws Exception {

		Airline al = SystemData.getAirline(AIRLINE_CODE);
		assertNotNull(al);
		log.info("Loading Flights for {}", al.getName());
		
		// Get the effective date
		LocalDate ld = LocalDate.now().plusDays(14);
		
		// Get the caches
		final CacheableCollection<Airport> processedAirports = new CacheableSet<Airport>(KEY);
		FileCache<CacheableCollection<Airport>> _apCache = new FileCache<CacheableCollection<Airport>>(2, _c1);
		FileCache<CacheableCollection<RawScheduleEntry>> _eCache = new FileCache<CacheableCollection<RawScheduleEntry>>(256, _c2);
		if (_apCache.size() > 0) {
			processedAirports.addAll(_apCache.get(KEY));
			log.info("Preloaded {} processed Airports", Integer.valueOf(processedAirports.size()));
		}
		
		// Get the Hub Airports
		GetRawScheduleInfo rsdao = new GetRawScheduleInfo(_c);
		Collection<Hub> hubs = rsdao.getHubs().stream().filter(h -> h.getAirline().equals(al)).collect(Collectors.toList());
		log.info("Loaded {} Hub Airports for {}", Integer.valueOf(hubs.size()), al.getName());
		
		// Load existing entries
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		for (Airport ap : processedAirports) {
			log.info("Reloading cached data for {}", ap.getIATA());
			CacheableCollection<RawScheduleEntry> cachedEntries = _eCache.get(ap.getIATA());
			if (cachedEntries != null) {
				log.info("Restored {} Schedule Entries for {}", Integer.valueOf(cachedEntries.size()), ap.getIATA());
				results.addAll(cachedEntries);
				hubs.remove(new Hub(al, ap));
			}
		}
		
		// Walk through the Hubs
		log.info("Processing {} Hub Airports", Integer.valueOf(hubs.size()));
		for (Hub h : hubs) {
			Collection<RawScheduleEntry> apEntries = loadFlights(h, ld);
			_eCache.add(new CacheableList<RawScheduleEntry>(h.getAirport().getIATA(), apEntries));
			processedAirports.add(h.getAirport());
			_apCache.add(processedAirports);
		}

		// Export to JSON file
		JSONScheduleFormatter fmt = new JSONScheduleFormatter();
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("C:\\Temp", String.format("avstack_%s.json", al.getCode().toLowerCase()))), 131072))) {
			pw.print(fmt.getHeader());
			for (Iterator<RawScheduleEntry> i = results.iterator(); i.hasNext(); ) {
				RawScheduleEntry rse = i.next();
				pw.print(fmt.format(rse));
				if (i.hasNext()) {
					pw.print(fmt.getSeparator());
					pw.println();
				}
			}
			
			pw.println(fmt.getFooter());
		}

		/* _apCache.clear();
		_eCache.clear(); */
	}
}
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
import org.deltava.dao.http.GetAviationStack;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadAVStackSchedule extends TestCase {
	
	private static final String DB = "dva";
	private static final String JDBC_URL = String.format("jdbc:mysql://sirius.sce.net/%s?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true", DB);
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
		_acTypes.addAll(acdao.getAircraftTypes());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}
	
	private Collection<RawScheduleEntry> loadFlights(Airline al, Airport ap, LocalDate ld) throws DAOException {
		
		// Get the API DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(17500);

		// Load the flights
		int ofs = 0;
		Collection<RawScheduleEntry> apEntries = new ArrayList<RawScheduleEntry>();
		log.info("Loading Departures for {} ({}) (ofs={})", ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
		PaginatedList<RawScheduleEntry> entries = avdao.get(ap, al, ld, true);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
		apEntries.addAll(entries);
		ThreadUtils.sleep(SLEEP_INTERVAL);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading Departures for{} ({}) (ofs={{})", ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(ap, al, ld, true, ofs);
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}
		
		return apEntries;
	}
	
	public void loadSchedule() throws Exception {

		Airline al = SystemData.getAirline(AIRLINE_CODE);
		assertNotNull(al);
		log.info("Loading Flights for {}", al.getName());
		
		// Get the effective date
		LocalDate ld = LocalDate.now().plusDays(7);
		
		// Get the caches
		final CacheableCollection<Airport> processedAirports = new CacheableSet<Airport>(KEY);
		FileCache<CacheableCollection<Airport>> _apCache = new FileCache<CacheableCollection<Airport>>(2, _c1);
		FileCache<CacheableCollection<RawScheduleEntry>> _eCache = new FileCache<CacheableCollection<RawScheduleEntry>>(2, _c2);
		if (_apCache.size() > 0) {
			log.info("Preloaded {} processed Airports", Integer.valueOf(_apCache.size()));
			processedAirports.addAll(_apCache.get(KEY));
		}
		
		// Get the popular airports
		SequencedCollection<Airport> airports = new LinkedHashSet<Airport>();
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		rsdao.getSources(true, DB);
		rsdao.setQueryMax(5);
		airports.addAll(rsdao.getPopularAirports(al, 5));
		
		// Get the API DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(17500);

		// Load the arrivals for the top airports
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		Collection<Airport> newArrivalAirports = new LinkedHashSet<Airport>();
		for (Airport dA : airports) {
			PaginatedList<RawScheduleEntry> entries = avdao.get(dA, al, ld, false);
			Thread.sleep(SLEEP_INTERVAL);
			Collection<Airport> naa = entries.stream().map(ScheduleEntry::getAirportD).filter(a -> !airports.contains(a)).collect(Collectors.toSet());
			log.info("Added {} new Airports to queue for {}", Integer.valueOf(naa.size()), dA.getIATA());
			newArrivalAirports.addAll(naa);
		}
		
		// Load existing entries
		for (Airport ap : processedAirports) {
			log.info("Reloading cached data for {}", ap.getIATA());
			CacheableCollection<RawScheduleEntry> cachedEntries = _eCache.get(ap.getIATA());
			if (cachedEntries != null) {
				log.info("Restored {} Schedule Entries for {}", Integer.valueOf(cachedEntries.size()), ap.getIATA());
				results.addAll(cachedEntries);
			}
		}

		// Walk through the airports. Load departures only
		log.info("Hub Airports for {} = {}", al.getName(), airports.stream().map(Airport::getIATA).collect(Collectors.toSet()));
		airports.addAll(newArrivalAirports);
		Airport ap = airports.isEmpty() ? null : airports.getFirst();
		while (ap != null) {
			Collection<RawScheduleEntry> apEntries = loadFlights(al, ap, ld);
			_eCache.add(new CacheableList<RawScheduleEntry>(ap.getIATA(), apEntries));

			// Get new airports
			Collection<Airport> newAirports = apEntries.stream().map(ScheduleEntry::getAirportA).filter(a -> !processedAirports.contains(a)).collect(Collectors.toSet());
			log.info("Added {} new Airports to queue for {}", Integer.valueOf(newAirports.size()), ap.getIATA());
			airports.addAll(newAirports);
			results.addAll(apEntries);

			// Update the airport lists
			airports.remove(ap);
			processedAirports.add(ap);
			_apCache.add(processedAirports);
			ap = airports.isEmpty() ? null : airports.getFirst();
		}

		// Export to JSON file
		JSONScheduleFormatter fmt = new JSONScheduleFormatter();
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("C:\\Temp", String.format("avstack_%s.json", al.getCode()))), 131072))) {
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

		_apCache.clear();
		_eCache.clear();
	}
}
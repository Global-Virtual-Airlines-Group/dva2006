package org.deltava;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

public class LoadAVStackSchedule extends ScheduleTestCase {
	
	private static final String AV_API_KEY = "foo";
	
	private static final int SLEEP_INTERVAL = 10_275;
	private static final Object KEY = "$KEY";
	
	private File _c1 = new File(System.getProperty("java.io.tmpdir"), "AirportCache.data");
	private File _c2 = new File(System.getProperty("java.io.tmpdir"), "ScheduleEntries.data");
	
	private static final int DAYS_FWD = 13;
	private static final String AIRLINE_CODE = "AM";

	private Collection<RawScheduleEntry> loadFlights(Hub h, LocalDate ld) throws DAOException {
		
		// Get the API DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(AV_API_KEY);
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(27500);
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
		
		// Get the effective date
		final LocalDate ld = LocalDate.now().plusDays(DAYS_FWD);
		final String dt = StringUtils.format(ld, "MM-dd-yyyy");
		
		// Get the Airline
		final Airline al = SystemData.getAirline(AIRLINE_CODE);
		assertNotNull(al);
		log.info("Loading Flights for {} on {}", al.getName(), dt);
		
		// Get the caches
		final CacheableCollection<Airport> processedAirports = new CacheableSet<Airport>(KEY);
		FileCache<CacheableCollection<Airport>> _apCache = new FileCache<CacheableCollection<Airport>>(2, _c1);
		FileCache<CacheableCollection<RawScheduleEntry>> _eCache = new FileCache<CacheableCollection<RawScheduleEntry>>(256, _c2);
		if (_apCache.size() > 0) {
			processedAirports.addAll(_apCache.get(KEY));
			log.info("Preloaded {} processed Airports", Integer.valueOf(processedAirports.size()));
		}
		
		// Get the Hub Airports
		Collection<Hub> hubs = _hubs.stream().filter(h -> h.getAirline().equals(al)).collect(Collectors.toList());
		log.info("Loaded {} Hub Airports for {}", Integer.valueOf(hubs.size()), al.getName());
		
		// Load existing entries
		Collection<RawScheduleEntry> results = new TreeSet<RawScheduleEntry>(ScheduleLegHelper.getDupeChecker(false));
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
		if (!hubs.isEmpty()) log.info("Processing {} Hub Airports", Integer.valueOf(hubs.size()));
		for (Hub h : hubs) {
			Collection<RawScheduleEntry> apEntries = loadFlights(h, ld);
			results.addAll(apEntries);
			_eCache.add(new CacheableList<RawScheduleEntry>(h.getAirport().getIATA(), apEntries));
			processedAirports.add(h.getAirport());
			_apCache.add(processedAirports);
		}
		
		// Export to JSON file
		JSONScheduleFormatter fmt = new JSONScheduleFormatter(); 
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("C:\\Temp", String.format("avstack_%s_%s.json", al.getCode().toLowerCase(), dt))), 131072))) {
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
		
		// Find and remove codeshares
		long csSize = results.stream().filter(ScheduleEntry::isCodeShare).count();
		log.info("Removing {} code shared flights", Long.valueOf(csSize));
		results.removeIf(ScheduleEntry::isCodeShare);
		
		// Write to database
		try (Connection con = getConnection()) {
			GetRawSchedule rsdao = new GetRawSchedule(con);
			List<RawScheduleEntry> rsEntries = rsdao.load(ScheduleSource.AVSTACK, null);
			
			// Get existing airline count
			int eeCount = rsEntries.size();
			rsEntries.removeIf(rse -> al.equals(rse.getAirline()) && ld.equals(rse.getStartDate()));
			log.info("Removed {} of {} existing entries", Integer.valueOf(eeCount - rsEntries.size()), Long.valueOf(eeCount));
			
			// Add new entries and update line numbers
			rsEntries.addAll(results);
			ScheduleLegHelper.calculateLineNumbers(rsEntries);
			
			// Write the entries
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : rsEntries)
				swdao.writeRaw(rse, false);
			
			con.commit();
			log.info("Wrote {} enries to database", Integer.valueOf(rsEntries.size()));
		}

		_apCache.clear();
		_eCache.clear();
		log.info("Complete");
	}
}
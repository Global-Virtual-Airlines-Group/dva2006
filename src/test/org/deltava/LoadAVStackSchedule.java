package org.deltava;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

public class LoadAVStackSchedule extends ScheduleTestCase {
	
	private static final String AV_API_KEY = "foo";
	
	private static final int SLEEP_INTERVAL = 10_275;
	private static final Object KEY = "$KEY";
	
	private File _c1 = new File(System.getProperty("java.io.tmpdir"), "AirportCache.data");
	private File _c2 = new File(System.getProperty("java.io.tmpdir"), "ScheduleEntries.data");
	
	private static final int DAYS_FWD = 14;

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
		log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
		PaginatedList<RawScheduleEntry> entries = avdao.get(h.getAirport(), h.getAirline(), ld, true);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), h.getAirport().getIATA());
		apEntries.addAll(entries);
		ThreadUtils.sleep(SLEEP_INTERVAL);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(h.getAirport(), h.getAirline(), ld, true, ofs);
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}
		
		// Load the arrival flights
		ofs = 0;
		log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
		entries = avdao.get(h.getAirport(), h.getAirline(), ld, false);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), h.getAirport().getIATA());
		apEntries.addAll(entries);
		ThreadUtils.sleep(SLEEP_INTERVAL);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA(), Integer.valueOf(ofs));
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
		log.info("Loading Flights on {}", dt);
		
		// Get the caches
		final CacheableCollection<Hub> processedHubs = new CacheableSet<Hub>(KEY);
		FileCache<CacheableCollection<Hub>> _hCache = new FileCache<CacheableCollection<Hub>>(2, _c1);
		FileCache<CacheableCollection<RawScheduleEntry>> _eCache = new FileCache<CacheableCollection<RawScheduleEntry>>(256, _c2);
		if (_hCache.size() > 0) {
			processedHubs.addAll(_hCache.get(KEY));
			log.info("Preloaded {} processed Hubs", Integer.valueOf(processedHubs.size()));
		}
		
		// Load existing entries
		Collection<RawScheduleEntry> results = new TreeSet<RawScheduleEntry>(RawScheduleHelper.getDupeChecker(false));
		for (Hub h : processedHubs) {
			log.info("Reloading cached data for {} / {}", h.getAirline().getCode(), h.getAirport().getIATA());
			CacheableCollection<RawScheduleEntry> cachedEntries = _eCache.get(h.cacheKey());
			if (cachedEntries != null) {
				log.info("Restored {} Schedule Entries for {} / {}", Integer.valueOf(cachedEntries.size()), h.getAirline().getCode(), h.getAirport().getIATA());
				results.addAll(cachedEntries);
				_hubs.remove(h);
			}
		}
		
		// Walk through the Hubs
		if (!_hubs.isEmpty()) log.info("Processing {} Hub Airports", Integer.valueOf(_hubs.size()));
		for (Hub h : _hubs) {
			Collection<RawScheduleEntry> apEntries = loadFlights(h, ld);
			results.addAll(apEntries);
			_eCache.add(new CacheableList<RawScheduleEntry>(h.getAirport().getIATA(), apEntries));
			processedHubs.add(h);
			_hCache.add(processedHubs);
		}
		
		// Export to JSON file
		JSONScheduleFormatter fmt = new JSONScheduleFormatter(); 
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("C:\\Temp", String.format("avstack_%s.json", dt))), 131072))) {
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

		_hCache.clear();
		_eCache.clear();
		log.info("Complete");
	}
}
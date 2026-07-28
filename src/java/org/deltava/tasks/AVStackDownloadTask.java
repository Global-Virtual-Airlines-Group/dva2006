// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.LocalDate;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to download partial Flight Schedules from AviationStack.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackDownloadTask extends Task {

	private static final int SLEEP_INTERVAL = 10_750;
	
	private static final Cache<CacheableCollection<RawScheduleEntry>> _eCache = CacheManager.getCollection(RawScheduleEntry.class, "AVStackEntries");
	
	public AVStackDownloadTask() {
		super("AviationStack Download", AVStackDownloadTask.class);
	}
	
	/*
	 * Helper method to load departure and arrival flights from a given Hub airport, handling pagination.
	 */
	private Collection<RawScheduleEntry> loadFlights(LocalDate dt, Hub h, Collection<Aircraft> acTypes) throws DAOException {
		boolean isLargeHub = (h.getDestinationCount() > 20);
		Collection<RawScheduleEntry> apEntries = new ArrayList<RawScheduleEntry>();
		
		// Get the AviationStack DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(29500);
		avdao.setCompression(Compression.GZIP, Compression.DEFLATE, Compression.BROTLI);
		avdao.setAircraft(acTypes);

		// Load Departures
		int ofs = 0; Airport ap = h.getAirport();
		log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
		PaginatedList<RawScheduleEntry> entries = avdao.get(ap, h.getAirline(), dt, true);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
		apEntries.addAll(entries);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(ap, h.getAirline(), dt, true, ofs);
			log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}
		
		// Check for empty result for large hub - this is usually an error
		if (isLargeHub && apEntries.isEmpty())
			return Collections.emptyList();

		// Load Arrivals
		ofs = 0;
		log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
		entries = avdao.get(ap, h.getAirline(), dt, false);
		log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
		apEntries.addAll(entries);
		while ((ofs + entries.getCount()) < entries.getTotal()) {
			ofs = entries.getOffset() + entries.getCount();
			log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
			entries = avdao.get(ap, h.getAirline(), dt, false, ofs);
			log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
			ThreadUtils.sleep(SLEEP_INTERVAL);
		}

		return apEntries;
	}

	/**
	 * Executes the Task.
	 * @param ctx the Task context
	 */
	@Override
	public void execute(TaskContext ctx) {

		// Get the effective date
		LocalDate ld = LocalDate.now().plusDays(14);

		// Load Hub airports and Airlines
		SequencedCollection<Hub> hubs = new LinkedHashSet<Hub>();
		Collection<Aircraft> acTypes = new ArrayList<Aircraft>();
		try {
			Connection con = ctx.getConnection();
			
			// Load Hubs
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(con);
			hubs.addAll(rsdao.getHubs());
			
			// Check for loaded airlines
			Collection<Airline> airlines = hubs.stream().map(Hub::getAirline).collect(Collectors.toCollection(TreeSet::new));
			for (Airline al : airlines) {
				if (rsdao.isLoaded(ScheduleSource.AVSTACK, al, ld)) {
					if (hubs.removeIf(h -> h.getAirline().equals(al)))
						log.info("Removing Hubs for already loaded {}", al.getName());
				}
			}
			
			// Load aircraft types
			GetAircraft acdao = new GetAircraft(con);
			acTypes.addAll(acdao.getAll());
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		// Walk through the Hubs. Load departures and arrivals
		boolean isComplete = true;
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try {
			log.info("Hub Airports = {}", hubs);
			for (Hub h : hubs) {
				CacheableCollection<RawScheduleEntry> entries = _eCache.get(h.toString());
				if (entries == null) {
					entries = new CacheableList<RawScheduleEntry>(h.toString());
					boolean isLargeHub = (h.getDestinationCount() > 20);					
					Collection<RawScheduleEntry> flights = loadFlights(ld, h, acTypes);
					if (!isLargeHub || !flights.isEmpty()) {
						entries.addAll(flights);
						_eCache.add(entries);
					} else {
						log.error("Returned no {} Flights for large Hub {} ({})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA());
						isComplete = false;
					}
				} else
					log.info("Retrieved {} {} flights for {} from cache", Integer.valueOf(entries.size()), h.getAirline().getName(), h.getAirport().getIATA());
						
				results.addAll(entries);
			}
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		}
		
		// Remove code shares
		int oldSize = results.size();
		if (results.removeIf(ScheduleEntry::isCodeShare))
			log.info("Removed {} code share flights", Integer.valueOf(oldSize - results.size()));

		// Eliminate duplicates
		Collection<RawScheduleEntry> rawEntries = new TreeSet<RawScheduleEntry>(ScheduleLegHelper.getDupeChecker(false));
		rawEntries.addAll(results);
		log.info("Eliminated {}/{} duplicate flights", Integer.valueOf(results.size() - rawEntries.size()), Integer.valueOf(results.size()));
		results.clear();
		if (!isComplete) {
			log.error("Aborting due to incomplete download");
			return;
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Load existing flights, and purge today's for this airline
			GetRawSchedule rsdao = new GetRawSchedule(con);
			rsdao.getSources(true, ctx.getDB());
			List<RawScheduleEntry> todaysFlights = rsdao.load(ScheduleSource.AVSTACK, null);
			todaysFlights.removeIf(rse -> rse.getStartDate().equals(ld));
			todaysFlights.addAll(rawEntries);
			
			// Update line numbers
			ScheduleLegHelper.calculateLineNumbers(todaysFlights);
			
			// Purge and save
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : todaysFlights)
				swdao.writeRaw(rse, false);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Complete");
	}
}
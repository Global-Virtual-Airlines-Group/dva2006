// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.time.*;
import java.util.*;
import java.sql.Connection;

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

	private static final Cache<CacheableCollection<RawScheduleEntry>> _eCache = CacheManager.getCollection(RawScheduleEntry.class, "AVStackEntries");
	
	/**
	 * Transfer class to track whether download is complete.
	 */
	private class APIResults extends ArrayList<RawScheduleEntry> {
		private boolean _isComplete;
		private boolean IsError;
		
		boolean isComplete() {
			return !IsError && _isComplete;
		}
	}

	/**
	 * Creates the Task.
	 */
	public AVStackDownloadTask() {
		super("AviationStack Download", AVStackDownloadTask.class);
	}
	
	/*
	 * Helper method to load departure and arrival flights from a given Hub airport, handling pagination.
	 */
	// FIXME: Eat the DAO exceptions, but track errors and mark as incomplete
	private APIResults loadFlights(LocalDate dt, Hub h, Collection<Aircraft> acTypes) {
		boolean isLargeHub = (h.getDestinationCount() > 15);
		APIResults apEntries = new APIResults();
		
		// Get the AviationStack DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(29500);
		avdao.setCompression(Compression.GZIP, Compression.DEFLATE, Compression.BROTLI);
		avdao.setAircraft(acTypes);

		// Load Departures
		Airport ap = h.getAirport(); final int SLEEP_TIME = SystemData.getInt("schedule.avstack.sleep", 60500);
		try {
			int ofs = 0;
			log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
			PaginatedList<RawScheduleEntry> entries = avdao.get(ap, h.getAirline(), dt, true, 0);
			log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_TIME));
			ThreadUtils.sleep(SLEEP_TIME);
			while ((ofs + entries.getCount()) < entries.getTotal()) {
				ofs = entries.getOffset() + entries.getCount();
				log.info("Loading {} Departures for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
				entries = avdao.get(ap, h.getAirline(), dt, true, ofs);
				log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
				apEntries.addAll(entries);
				log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_TIME));
				ThreadUtils.sleep(SLEEP_TIME);
			}
		} catch (DAOException de) {
			apEntries.IsError = true;
			int statusCode = (de instanceof HTTPDAOException hde) ? hde.getStatusCode() : 0;
			if (statusCode == 429) { // Triggered rate limit
				log.warn("Triggered AviationStack rate limit, pausing for 60s");
				ThreadUtils.sleep(60_500);
			} else
				log.warn("Error loading {} {} Departures - {}", h.getAirline().getCode(), ap.getIATA(), de.getMessage());
		}
		
		// Check for empty result for large hub - this is usually an error
		if (isLargeHub && apEntries.isEmpty())
			return apEntries;

		// Load Arrivals
		try {
			int ofs = 0;
			log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
			PaginatedList<RawScheduleEntry> entries = avdao.get(ap, h.getAirline(), dt, false, 0);
			log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_TIME));
			ThreadUtils.sleep(SLEEP_TIME);
			while ((ofs + entries.getCount()) < entries.getTotal()) {
				ofs = entries.getOffset() + entries.getCount();
				log.info("Loading {} Arrivals for {} ({}) (ofs={})", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
				entries = avdao.get(ap, h.getAirline(), dt, false, ofs);
				log.info("Loaded {}/{} flights for {}", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
				apEntries.addAll(entries);
				log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_TIME));
				ThreadUtils.sleep(SLEEP_TIME);
			}
			
			apEntries._isComplete = !apEntries.IsError;
		} catch (DAOException de) {
			apEntries.IsError = true;
			int statusCode = (de instanceof HTTPDAOException hde) ? hde.getStatusCode() : 0;
			if (statusCode == 429) { // Triggered rate limit
				log.warn("Triggered AviationStack rate limit, pausing for 60s");
				ThreadUtils.sleep(60_500);
			} else
				log.warn("Error loading {} {} Arrivals - {}", h.getAirline().getCode(), ap.getIATA(), de.getMessage());
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
		log.warn("Loading Schedules for {}", StringUtils.format(ld, "MM/dd/yyyy"));

		// Load Hub airports and Airlines
		SequencedCollection<Hub> hubs = new LinkedHashSet<Hub>();
		Collection<Aircraft> acTypes = new ArrayList<Aircraft>();
		try {
			Connection con = ctx.getConnection();
			
			// Check if we're complete
			GetMetadata mddao = new GetMetadata(con);
			Instant lastLoad = StringUtils.parseEpoch(mddao.get(String.format("%s.avstack.imort", SystemData.get("airline.code")), "0"));
			LocalDate lld = lastLoad.atZone(ZoneOffset.UTC).toLocalDate();
			if (lld.equals(ld)) {
				log.info("Already loaded AviationStack flights for {}", StringUtils.format(ld, "MM/dd/yyyy"));
				return;
			}
			
			// Load Hubs
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(con);
			hubs.addAll(rsdao.getHubs());
			
			// Load aircraft types for IATA/ICAO lookup
			GetAircraft acdao = new GetAircraft(con);
			acTypes.addAll(acdao.getAircraftTypes());
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		// Walk through the Hubs. Load departures and arrivals
		boolean isComplete = true;
		List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		log.info("Hub Airports = {}", hubs);
		for (Hub h : hubs) {
			CacheableCollection<RawScheduleEntry> entries = _eCache.get(h.toString());
			if (entries == null) {
				entries = new CacheableList<RawScheduleEntry>(h.toString());
				APIResults flights = loadFlights(ld, h, acTypes);
				isComplete &= flights.isComplete();
				if (flights.isComplete()) {
					entries.addAll(flights);
					_eCache.add(entries);
				} else
					log.error("Returned no {} Flights for large Hub {} ({})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA());
			} else
				log.info("Retrieved {} {} flights for {} from cache", Integer.valueOf(entries.size()), h.getAirline().getName(), h.getAirport().getIATA());
					
			results.addAll(entries);
		}
		
		// Merge code shares
		RawScheduleHelper.mergeCodeShares(results);
		
		// Eliminate duplicates
		Collection<RawScheduleEntry> rawEntries = new TreeSet<RawScheduleEntry>(RawScheduleHelper.getDupeChecker(false));
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
			
			// Clean out processed hubs
			for (Hub h : hubs) {
				if (todaysFlights.removeIf(rse -> rse.getStartDate().equals(ld) && h.hasRoute(rse)))
					log.info("Removing {} Flights for {} ({})", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA());
			}
			
			// Remove older flights
			final LocalDate today = LocalDate.now();
			todaysFlights.removeIf(rse -> rse.getEndDate().isBefore(today));
			
			// Update line numbers
			todaysFlights.addAll(rawEntries);
			RawScheduleHelper.calculateLineNumbers(todaysFlights);
			
			// Purge and save
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : todaysFlights)
				swdao.writeRaw(rse, false);
			
			// Write metadata and commit
			SetMetadata mdwdao = new SetMetadata(con);
			mdwdao.write(String.format("%s.avstack.imort", SystemData.get("airline.code")), String.valueOf(Instant.now().toEpochMilli()));
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
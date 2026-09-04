// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.logging.log4j.Level;

import org.deltava.beans.LogEntry;
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

	private static final Cache<CacheableCollection<LogEntry>> _statusCache = CacheManager.getCollection(LogEntry.class, "AVStackStatus");
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
	private APIResults loadFlights(TaskContext ctx, LocalDate dt, Hub h, Collection<Aircraft> acTypes) {
		boolean isLargeHub = (h.getDestinationCount() > 15);
		APIResults apEntries = new APIResults();
		
		// Get the AviationStack DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(4500);
		avdao.setReadTimeout(29500);
		avdao.setCompression(Compression.GZIP, Compression.DEFLATE, Compression.BROTLI);
		avdao.setAircraft(acTypes);

		// Load Departures
		Airport ap = h.getAirport(); final int SLEEP_TIME = SystemData.getInt("schedule.avstack.sleep", 60500);
		try {
			int ofs = 0;
			ctx.log(Level.INFO, "Loading %s Departures for %s (%s) (ofs=0)", h.getAirline().getCode(), ap.getName(), ap.getIATA());
			PaginatedList<RawScheduleEntry> entries = avdao.get(ap, h.getAirline(), dt, true, 0);
			ctx.log(Level.INFO, "Loaded %d/%d flights for %s", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			ctx.log(Level.INFO, "Sleeping for %d ms", Integer.valueOf(SLEEP_TIME));
			ThreadUtils.sleep(SLEEP_TIME);
			while ((ofs + entries.getCount()) < entries.getTotal()) {
				ofs = entries.getOffset() + entries.getCount();
				ctx.log(Level.INFO, "Loading %s Departures for %s (%s) (ofs=%d)", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
				entries = avdao.get(ap, h.getAirline(), dt, true, ofs);
				ctx.log(Level.INFO, "Loaded %d/%d flights for %s", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
				apEntries.addAll(entries);
				ctx.log(Level.INFO, "Sleeping for %d ms", Integer.valueOf(SLEEP_TIME));
				ThreadUtils.sleep(SLEEP_TIME);
			}
		} catch (DAOException de) {
			apEntries.IsError = true;
			int statusCode = (de instanceof HTTPDAOException hde) ? hde.getStatusCode() : 0;
			if (statusCode == 429) { // Triggered rate limit
				ctx.log(Level.WARN, "Triggered AviationStack rate limit, pausing for 60s");
				ThreadUtils.sleep(60_500);
			} else {
				ctx.log(Level.ERROR, "%s loading %s %s Departures - %s", de.getClass().getSimpleName(), h.getAirline().getCode(), ap.getIATA(), de.getMessage());
				ThreadUtils.sleep(SLEEP_TIME);
			}
		}
		
		// Check for empty result for large hub - this is usually an error
		if (isLargeHub && apEntries.isEmpty()) {
			ctx.log(Level.WARN, "Zero entries for %s %s (%s), assuming error", h.getAirline().getCode(), ap.getName(), ap.getIATA());
			apEntries.IsError = true;
		}

		// Load Arrivals
		try {
			int ofs = 0;
			ctx.log(Level.INFO, "Loading %s Arrivals for %s (%s) (ofs=0)", h.getAirline().getCode(), ap.getName(), ap.getIATA());
			PaginatedList<RawScheduleEntry> entries = avdao.get(ap, h.getAirline(), dt, false, 0);
			ctx.log(Level.INFO, "Loaded %d/%d flights for %s", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
			apEntries.addAll(entries);
			ctx.log(Level.INFO, "Sleeping for %d ms", Integer.valueOf(SLEEP_TIME));
			ThreadUtils.sleep(SLEEP_TIME);
			while ((ofs + entries.getCount()) < entries.getTotal()) {
				ofs = entries.getOffset() + entries.getCount();
				ctx.log(Level.INFO, "Loading %s Arrivals for %s (%s) (ofs=%d)", h.getAirline().getCode(), ap.getName(), ap.getIATA(), Integer.valueOf(ofs));
				entries = avdao.get(ap, h.getAirline(), dt, false, ofs);
				ctx.log(Level.INFO, "Loaded %d/%d flights for %s", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA());
				apEntries.addAll(entries);
				ctx.log(Level.INFO, "Sleeping for %d ms", Integer.valueOf(SLEEP_TIME));
				ThreadUtils.sleep(SLEEP_TIME);
			}
			
			apEntries._isComplete = !apEntries.IsError;
		} catch (DAOException de) {
			apEntries.IsError = true;
			int statusCode = (de instanceof HTTPDAOException hde) ? hde.getStatusCode() : 0;
			if (statusCode == 429) { // Triggered rate limit
				ctx.log(Level.WARN, "Triggered AviationStack rate limit, pausing for 60s");
				ThreadUtils.sleep(60_500);
			} else {
				ctx.log(Level.ERROR, "%s loading %s %s Arrival - %s", de.getClass().getSimpleName(), h.getAirline().getCode(), ap.getIATA(), de.getMessage());
				ThreadUtils.sleep(SLEEP_TIME);
			}
		}

		return apEntries;
	}

	@Override
	public void execute(TaskContext ctx) {

		// Get the effective date
		IntervalTaskTimer tt = new IntervalTaskTimer();
		LocalDate ld = LocalDate.now().plusDays(SystemData.getInt("schedule.avstack.days", 14));
		ctx.log(Level.WARN, "Loading %s Schedules for %s", SystemData.get("airline.code"), StringUtils.format(ld, "MM/dd/yyyy"));

		// Load Hub airports and Airlines
		SequencedCollection<Hub> hubs = new LinkedHashSet<Hub>();
		Collection<Aircraft> acTypes = new ArrayList<Aircraft>();
		try {
			Connection con = ctx.getConnection();
			
			// Check if we're complete
			GetMetadata mddao = new GetMetadata(con);
			Instant lastLoad = mddao.getDate(String.format("%s.avstack.import", SystemData.get("airline.code").toLowerCase()));
			Duration d = (lastLoad == null) ? Duration.MAX : Duration.between(lastLoad, Instant.now());
			if (d.toHours() < 20) {
				ctx.log(Level.INFO, "Already loaded AviationStack flights for %s (%d hours)", StringUtils.format(ld, "MM/dd/yyyy"), Long.valueOf(d.toHours()));
				return;
			} else if (lastLoad != null)
				ctx.log(Level.INFO, "Last AviationStack import on %s UTC", StringUtils.format(lastLoad, "MM/dd/yyyy HH:mm"));
			
			// Load Hubs
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(con);
			hubs.addAll(rsdao.getHubs());
			
			// Load aircraft types for IATA/ICAO lookup
			GetAircraft acdao = new GetAircraft(con);
			acTypes.addAll(acdao.getAircraftTypes());
			tt.mark("dbData");
		} catch (DAOException de) {
			ctx.log(Level.ERROR, de.getMessage());
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		// Walk through the Hubs. Load departures and arrivals
		boolean isComplete = true;
		List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		ctx.log(Level.INFO, "Hub Airports = %s", hubs);
		for (Hub h : hubs) {
			CacheableCollection<RawScheduleEntry> entries = _eCache.get(h.toString());
			if (entries == null) {
				entries = new CacheableList<RawScheduleEntry>(h.toString());
				APIResults flights = loadFlights(ctx, ld, h, acTypes);
				tt.mark(h.toString());
				isComplete &= flights.isComplete();
				if (flights.isComplete()) {
					entries.addAll(flights);
					_eCache.add(entries);
				} else
					ctx.log(Level.WARN, "Returned no %s Flights for large Hub %s (%s)", h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA());
			} else
				ctx.log(Level.INFO, "Retrieved %d %s flights for %s from cache", Integer.valueOf(entries.size()), h.getAirline().getName(), h.getAirport().getIATA());
					
			results.addAll(entries);
		}

		// Check if we're complete
		if (!isComplete) {
			ctx.log(Level.ERROR, "Aborting due to incomplete download");
			return;
		}
		
		// Get operators and codeshares to use
		Collection<String> opCodes = hubs.stream().map(h -> h.getAirline().getCode()).collect(Collectors.toSet());
		Collection<String> csCodes = SystemData.getCollection(String.class, "schedule.avstack.codeshares");
		if (csCodes == null) csCodes = Collections.emptySet();
		
		// Merge code shares
		CodeShareFilter csf = new CodeShareFilter(opCodes, csCodes);
		ctx.log(Level.INFO, "Code Share Operators = %s, Marketers = %s", csf.getOperatorCodes(), csf.getMarketerCodes());
		RawScheduleHelper.mergeCodeShares(results, csf);
		
		// Eliminate duplicates
		Collection<RawScheduleEntry> rawEntries = new TreeSet<RawScheduleEntry>(RawScheduleHelper.getDupeChecker(false));
		rawEntries.addAll(results);
		tt.mark("dupes");
		ctx.log(Level.INFO, "Eliminated %d/%d duplicate flights", Integer.valueOf(results.size() - rawEntries.size()), Integer.valueOf(results.size()));
		results.clear();
		
		// Adjust equipment codes
		int eqCnt = 0;
		for (RawScheduleEntry rse : rawEntries) {
			if ("B767-300".equals(rse.getEquipmentType())) {
				rse.setEquipmentType("B767-300ER");
				eqCnt++;
			} else if ("B737-900".equals(rse.getEquipmentType()) && "DL".equals(rse.getAirline().getCode())) {
				rse.setEquipmentType("B737-900ER");
				eqCnt++;
			}
		}
		
		tt.mark("eqMassage");
		ctx.log(Level.INFO, "Adjusted %d equipment codes", Integer.valueOf(eqCnt));
		
		try {
			Connection con = ctx.getConnection();
			
			// Load existing flights, and purge today's for this airline
			GetRawSchedule rsdao = new GetRawSchedule(con);
			rsdao.getSources(true, ctx.getDB());
			List<RawScheduleEntry> todaysFlights = rsdao.load(ScheduleSource.AVSTACK, null);
			tt.mark("dbRead");
			
			// Clean out processed hubs
			for (Hub h : hubs) {
				int size = todaysFlights.size();
				if (todaysFlights.removeIf(rse -> rse.getStartDate().equals(ld) && h.hasRoute(rse))) {
					ctx.log(Level.INFO, "Removing %d %s Flights for %s (%s)", Integer.valueOf(size - todaysFlights.size()), h.getAirline().getCode(), h.getAirport().getName(), h.getAirport().getIATA());
				}
			}
			
			// Remove older flights
			final LocalDate yesterday = LocalDate.now().minusDays(1);
			todaysFlights.removeIf(rse -> rse.getEndDate().isBefore(yesterday));
			tt.mark("removeExpired");
			
			// Update line numbers
			todaysFlights.addAll(rawEntries);
			RawScheduleHelper.calculateLineNumbers(todaysFlights);
			tt.mark("lineNumber");
			
			// Purge and save
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : todaysFlights)
				swdao.writeRaw(rse, false);
			
			// Write metadata and commit
			SetMetadata mdwdao = new SetMetadata(con);
			mdwdao.write(String.format("%s.avstack.import", SystemData.get("airline.code").toLowerCase()), Instant.now());
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			ctx.log(Level.ERROR, de.getMessage());
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			tt.stop();
			ctx.release();
		}

		ctx.log(Level.INFO, tt.toString());
		ctx.log(Level.INFO, "Complete");
		
		// Save log to cache
		CacheableList<LogEntry> entries = new CacheableList<LogEntry>(SystemData.get("airline.code"));
		entries.addAll(ctx.getLogEntries());
		_statusCache.add(entries);
	}
}
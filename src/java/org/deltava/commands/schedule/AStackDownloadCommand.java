// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.LocalDate;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.GetAviationStack;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to download partial Flight Schedules from AviationStack.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

// TODO: Turn into a Scheduled Task
public class AStackDownloadCommand extends AbstractCommand {

	private static final int SLEEP_INTERVAL = 62_500;
	private static final Logger log = LogManager.getLogger(AStackDownloadCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Airline
		CommandResult result = ctx.getResult();
		Airline al = SystemData.getAirline(ctx.getParameter("airline"));
		if (al == null) {
			result.setURL("/jsp/schedule/aStackDL.jsp");
			result.setSuccess(true);
			return;
		}

		// Get the effective date
		LocalDate ld = LocalDate.now().plusDays(7);
		Collection<String> msgs = new ArrayList<String>();

		// Load initial airports
		SequencedCollection<Airport> airports = new LinkedHashSet<Airport>();
		try {
			Connection con = ctx.getConnection();

			// Get the popular airports
			GetRawSchedule rsdao = new GetRawSchedule(con);
			rsdao.getSources(true, ctx.getDB());
			rsdao.setQueryMax(5);
			airports.addAll(rsdao.getPopularAirports(al, 5));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Get the API DAO
		GetAviationStack avdao = new GetAviationStack();
		avdao.setAccessKey(SystemData.get("security.key.avstack"));
		avdao.setConnectTimeout(3500);
		avdao.setReadTimeout(17500);

		// Load the arrivals for the top airports
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try {
			for (Airport dA : airports) {
				PaginatedList<RawScheduleEntry> entries = avdao.get(dA, al, ld, false);
				Collection<Airport> newAirports = entries.stream().map(ScheduleEntry::getAirportD).filter(a -> !airports.contains(a)).collect(Collectors.toSet());
				log(String.format("Added %d new Airports to queue for %d", Integer.valueOf(newAirports.size()), dA.getIATA()), msgs);
			}

			// Walk through the airports. Load departures only
			log(String.format("Hub Airports for %s = %s", al.getName(), airports.stream().map(Airport::getIATA).collect(Collectors.toSet())), msgs);
			Collection<Airport> processedAirports = new LinkedHashSet<Airport>();
			Airport ap = airports.isEmpty() ? null : airports.getFirst();
			while (ap != null) {
				int ofs = 0;
				Collection<RawScheduleEntry> apEntries = new ArrayList<RawScheduleEntry>();
				log(String.format("Loading Departures for %s (%s) (ofs=%d)", ap.getName(), ap.getIATA(), Integer.valueOf(ofs)), msgs);
				PaginatedList<RawScheduleEntry> entries = avdao.get(ap, al, ld, true);
				log(String.format("Loaded %d/%d flights for %s", Integer.valueOf(entries.getCount()), Integer.valueOf(entries.getTotal()), ap.getIATA()), msgs);
				apEntries.addAll(entries);
				while ((ofs + entries.getCount()) < entries.getTotal()) {
					ofs = entries.getOffset() + entries.getCount();
					log(String.format("Loading Departures for %s (%s) (ofs={%d)", ap.getName(), ap.getIATA(), Integer.valueOf(ofs)), msgs);
					entries = avdao.get(ap, al, ld, true, ofs);
					apEntries.addAll(entries);
					log.info("Sleeping for {}ms", Integer.valueOf(SLEEP_INTERVAL));
					ThreadUtils.sleep(SLEEP_INTERVAL);
				}

				// Get new airports
				Collection<Airport> newAirports = apEntries.stream().map(ScheduleEntry::getAirportA).filter(a -> !processedAirports.contains(a)).collect(Collectors.toSet());
				log(String.format("Added %d new Airports to queue for %d", Integer.valueOf(newAirports.size()), ap.getIATA()), msgs);
				airports.addAll(newAirports);
				results.addAll(apEntries);

				// Update the airport lists
				airports.remove(ap);
				processedAirports.add(ap);
				ap = airports.isEmpty() ? null : airports.getFirst();
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		}

		// Eliminate duplicates
		Collection<RawScheduleEntry> rawEntries = new TreeSet<RawScheduleEntry>(ScheduleLegHelper.getDupeChecker(false));
		rawEntries.addAll(results);
		log(String.format("Eliminated %d/%d duplicate flights", Integer.valueOf(results.size() - rawEntries.size()), Integer.valueOf(results.size())), msgs);
		results.clear();
		
		try {
			Connection con = ctx.getConnection();
			
			// Load existing flights, and purge today's for this airline
			GetRawSchedule rsdao = new GetRawSchedule(con);
			rsdao.getSources(true, ctx.getDB());
			List<RawScheduleEntry> todaysFlights = rsdao.load(ScheduleSource.AVSTACK, null);
			todaysFlights.removeIf(rse -> (rse.getAirline().equals(al) && (rse.getDays().contains(ld.getDayOfWeek()))));
			todaysFlights.addAll(rawEntries);
			
			// Update line numbers
			for (int ln = 0; ln < todaysFlights.size(); ln++) {
				RawScheduleEntry rse = todaysFlights.get(ln);
				rse.setLineNumber(ln);
			}
			
			// Purge and save
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : todaysFlights)
				swdao.writeRaw(rse, false);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/schedule/aStackDL.jsp");
		result.setSuccess(true);
	}

	/*
	 * Helper method to write to both the log and the status message collection.
	 */
	private static void log(String msg, Collection<String> msgs) {
		log.info(msg);
		msgs.add(msg);
	}
}
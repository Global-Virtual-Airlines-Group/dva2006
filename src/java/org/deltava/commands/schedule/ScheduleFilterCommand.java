// Copyright 2006, 2009, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save imported Flight Schedule data to the database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleFilterCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(ScheduleFilterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Load schedule sources
		CommandResult result = ctx.getResult();
		try {
			GetRawSchedule rsdao = new GetRawSchedule(ctx.getConnection());
			ctx.setAttribute("sources", rsdao.getSources(), REQUEST);
			ctx.setAttribute("srcAirlines", rsdao.getSourceAirlines(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// If we're doing a get, then redirect to the JSP
		if (ctx.getParameters("src") == null) {
			ctx.setAttribute("today", LocalDate.now(), REQUEST);
			result.setURL("/jsp/schedule/flightFilter.jsp");
			result.setSuccess(true);
			return;
		}

		// Load import options
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		boolean canPurge = Boolean.valueOf(ctx.getParameter("canPurge")).booleanValue();
		LocalDate effectiveDate = LocalDate.ofInstant(StringUtils.parseInstant(ctx.getParameter("effDate"), "MM/dd/yyyy"), ZoneOffset.UTC);
		Collection<ScheduleSource> sources = ctx.getParameters("src", Collections.emptyList()).stream().map(src -> ScheduleSource.valueOf(src)).collect(Collectors.toCollection(TreeSet::new));
		
		// Load source/airline mappings
		Map<ScheduleSource, Collection<Airline>> srcAirlines = new HashMap<ScheduleSource, Collection<Airline>>();
		for (ScheduleSource src : sources) {
			Collection<String> srcCodes = ctx.getParameters("airline-" + src.name(), Collections.emptyList()); 
			srcAirlines.put(src, srcCodes.stream().map(ac -> SystemData.getAirline(ac)).filter(Objects::nonNull).collect(Collectors.toSet()));
		}

		// Save the entries
		Set<RawScheduleEntry> entries = new LinkedHashSet<RawScheduleEntry>();
		try {
			Connection con = ctx.getConnection();

			// Start the transaction
			ctx.startTX();
			
			// Load the entries, save source mappings
			SetSchedule dao = new SetSchedule(con);
			GetRawSchedule rawdao = new GetRawSchedule(con);
			for (ScheduleSource src : sources) {
				Collection<Airline> validAirlines = srcAirlines.getOrDefault(src, Collections.emptyList());
				Collection<RawScheduleEntry> srcEntries = rawdao.load(src, effectiveDate).stream().filter(se -> validAirlines.contains(se.getAirline())).collect(Collectors.toList());
				for (Iterator<RawScheduleEntry> i = srcEntries.iterator(); i.hasNext(); ) {
					RawScheduleEntry rse = i.next();
					rse.setCanPurge(canPurge);
					boolean isAdded = entries.add(rse);
					if (!isAdded) {
						log.info(rse.getShortCode() + " already exists");
						i.remove();
					}
				}
				
				log.info("Loaded " + srcEntries.size() + " " + src.getDescription() + " schedule entries for " + effectiveDate);
				dao.writeSourceAirlines(src, validAirlines);
			}

			// Save the schedule entries
			if (doPurge) dao.purge(false);
			for (RawScheduleEntry rse : entries)
				dao.write(rse, false);
			
			// Save effective date
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.write(aCode + ".schedule.effDate", effectiveDate.atStartOfDay().toInstant(ZoneOffset.UTC));
			
			// Determine unserviced airports
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			AirportServiceMap svcMap = sidao.getRoutePairs();
			SetAirportAirline awdao = new SetAirportAirline(con);
			synchronized (SystemData.class) {
				Collection<Airport> allAirports = SystemData.getAirports().values();
				for (Airport ap : allAirports) {
					Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
					if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
						log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
						ap.setAirlines(svcMap.getAirlineCodes(ap));
						awdao.update(ap, ap.getIATA());
					}
				}
			}

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the status
		ctx.setAttribute("isFlights", Boolean.TRUE, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(entries.size()), REQUEST);
		ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}
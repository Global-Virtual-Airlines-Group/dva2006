// Copyright 2006, 2009, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;

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

		// If we're doing a get, then redirect to the JSP
		CommandResult result = ctx.getResult();
		if (ctx.getParameters("src") == null) {
			result.setURL("/jsp/schedule/flightFilter.jsp");
			result.setSuccess(true);
			return;
		}

		// Load import options
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		boolean canPurge = Boolean.valueOf(ctx.getParameter("canPurge")).booleanValue();
		boolean isHistoric = Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue();
		LocalDate effectiveDate = LocalDate.ofInstant(StringUtils.parseInstant(ctx.getParameter("effDate"), "MM/dd/yyyy"), ZoneOffset.UTC);
		Collection<ScheduleSource> sources = ctx.getParameters("src", Collections.emptyList()).stream().map(src -> ScheduleSource.valueOf(src)).collect(Collectors.toSet());

		// Save the entries
		Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		try {
			Connection con = ctx.getConnection();
			
			// Load the entries
			GetRawSchedule rawdao = new GetRawSchedule(con);
			for (ScheduleSource src : sources) {
				LocalDate srcDate = effectiveDate;
				if (src == ScheduleSource.INNOVATA) {
					String dt = SystemData.get("schedule.innovata.import.replayDate");
					LocalDate ldt = LocalDate.ofInstant(StringUtils.parseInstant(dt, "MM/dd/yyyy"), ZoneOffset.UTC);
					int daysToAdjust = effectiveDate.get(ChronoField.DAY_OF_WEEK) - 1;
					srcDate = ldt.plusDays(daysToAdjust);
				}
				
				Collection<RawScheduleEntry> srcEntries = rawdao.load(src, srcDate);
				entries.addAll(srcEntries);
				log.info("Loaded " + srcEntries.size() + " " + src.getDescription() + " schedule entries for " + srcDate);
			}

			// Start the transaction
			ctx.startTX();

			// Get the DAO and purge if requested
			SetSchedule dao = new SetSchedule(con);
			if (doPurge)
				dao.purge(false);

			// Save the schedule entries
			for (Iterator<?> i = entries.iterator(); i.hasNext();) {
				ScheduleEntry se = (ScheduleEntry) i.next();
				se.setCanPurge(canPurge);
				se.setHistoric(isHistoric);
				dao.write(se, false);
			}

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

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Clean up the session
		ctx.getSession().removeAttribute("entries");
		ctx.getSession().removeAttribute("errors");
		ctx.getSession().removeAttribute("schedType");

		// Save the status
		ctx.setAttribute("isFlights", Boolean.TRUE, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(entries.size()), REQUEST);
		ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}
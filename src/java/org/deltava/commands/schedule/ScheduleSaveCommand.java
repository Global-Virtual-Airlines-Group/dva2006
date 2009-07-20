// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save imported Flight Schedule data to the database.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ScheduleSaveCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(ScheduleSaveCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get command results - redirect if no results loaded
		Collection<?> entries = (Collection<?>) ctx.getSession().getAttribute("entries");
		CommandResult result = ctx.getResult();
		if (entries == null) {
			result.setURL("schedimport", null, null);
			result.setType(ResultType.REDIRECT);
			result.setSuccess(true);
			return;
		}

		// If we're doing a get, then redirect to the JSP
		if (ctx.getParameter("doImport") == null) {
			result.setURL("/jsp/schedule/flightSave.jsp");
			result.setSuccess(true);
			return;
		}

		// Load import options
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		boolean canPurge = Boolean.valueOf(ctx.getParameter("canPurge")).booleanValue();
		boolean isHistoric = Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue();
		boolean updateAirports = Boolean.valueOf(ctx.getParameter("updateAirports")).booleanValue();

		// Save the entries
		try {
			Connection con = ctx.getConnection();

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
			if (updateAirports) {
				GetScheduleInfo sidao = new GetScheduleInfo(con);
				AirportServiceMap svcMap = sidao.getRoutePairs();
				
				synchronized (SystemData.class) {
					Collection<Airport> allAirports = SystemData.getAirports().values();
					for (Iterator<Airport> i = allAirports.iterator(); i.hasNext();) {
						Airport ap = i.next();
						Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
						if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
							log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
							ap.setAirlines(svcMap.getAirlineCodes(ap));
							dao.update(ap, ap.getIATA());
						}
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
		ctx.setAttribute("entryCount", new Integer(entries.size()), REQUEST);
		ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}
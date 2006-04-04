// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to save imported Flight Schedule data to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command results - redirect if no results loaded
		Collection entries = (Collection) ctx.getSession().getAttribute("entries");
		CommandResult result = ctx.getResult();
		if (entries == null) {
			result.setURL("schedimport", null, null);
			result.setType(CommandResult.REDIRECT);
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
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				ScheduleEntry se = (ScheduleEntry) i.next();
				se.setPurge(canPurge);
				se.setHistoric(isHistoric);
				dao.write(se, false);
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
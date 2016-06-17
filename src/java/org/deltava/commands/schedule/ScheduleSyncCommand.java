// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to synchronize flight schedules between Airlines.
 * @author Luke
 * @version 7.0
 * @since 6.0
 */

public class ScheduleSyncCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Copy request attributes
		ctx.setAttribute("apps", SystemData.getApps().stream().filter(a -> !a.getCode().equals(SystemData.get("airline.code"))).collect(Collectors.toList()), REQUEST);
		ctx.setAttribute("airlines", SystemData.getAirlines().values().stream().filter(a -> (a.getActive() && a.getScheduleSync())).collect(Collectors.toList()), REQUEST);

		// Check for execution
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/scheduleSync.jsp");
		Airline al = SystemData.getAirline(ctx.getParameter("airline"));
		if (al == null) {
			result.setSuccess(true);
			return;
		}
		
		ctx.setAttribute("airline", al, REQUEST);
		
		// Get source web app
		AirlineInformation ai = SystemData.getApp(ctx.getParameter("vaCode"));
		try {
			if (ai == null)
				throw new IllegalStateException("Unknown Virtual Airline - " + ctx.getParameter("vaCode"));
			if (!al.getApplications().contains(ai.getCode()))
				throw new IllegalStateException(al.getName() + " is not used by " + ai.getName());
			if (!al.getActive())
				throw new IllegalStateException(al.getName() + " is not currently active");
			if (!al.getScheduleSync())
				throw new IllegalStateException(al.getName() + " does not allow schedule synchronization");
		} catch (IllegalStateException ise) {
			ctx.setMessage(ise.getMessage());
			result.setSuccess(true);
			return;
		}
		
		boolean purgeOnly = Boolean.valueOf(ctx.getParameter("purgeOnly")).booleanValue();
		boolean purgeEntries = Boolean.valueOf(ctx.getParameter("purgeEntries")).booleanValue();
		try { 
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Purge and copy
			SetScheduleSync swdao = new SetScheduleSync(con);
			ctx.setAttribute("entriesPurged", Integer.valueOf(swdao.purge(al)), REQUEST);
			if (!purgeOnly)
				ctx.setAttribute("entriesCopied", Integer.valueOf(swdao.copy(al, purgeEntries, ai.getDB())), REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("src", ai, REQUEST);
		ctx.setAttribute("scheduleSync", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
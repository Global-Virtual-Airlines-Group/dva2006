// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.FlightCodeParser;

/**
 * A Web Site Command to delete Flight Schedule entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the old flight ID
		String fCode = (String) ctx.getCmdParameter(ID, null);
		ScheduleEntry id = FlightCodeParser.parse(fCode);
		if (id == null)
			throw notFoundException("Invalid Flight Code - " + fCode);

		// Check our access
		ScheduleAccessControl ac = new ScheduleAccessControl(ctx);
		ac.validate();
		if (!ac.getCanDelete())
			throw securityException("Cannot modify Flight Schedule");

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the entry
			GetSchedule dao = new GetSchedule(con);
			ScheduleEntry entry = dao.get(id);
			if (entry == null)
				throw notFoundException("Invalid Flight Code - " + fCode);

			// Delete the entry
			SetSchedule wdao = new SetSchedule(con);
			wdao.delete(entry);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		ctx.setAttribute("scheduleEntry", id, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}
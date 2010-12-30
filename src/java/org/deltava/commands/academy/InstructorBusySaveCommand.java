// Copyright 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import org.deltava.beans.academy.InstructionBusy;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save a new busy time for a Flight Academy Instructor.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class InstructorBusySaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		BusyTimeAccessControl ac = new BusyTimeAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanCreate())
			throw securityException("Cannot create Instructor busy time entry");
		
		// Check if we're proxying for someone else
		int id = ctx.getUser().getID();
		if (ac.getCanProxyCreate())
			id = StringUtils.parse(ctx.getParameter("instructor"), ctx.getUser().getID());

		// Build the bean
		InstructionBusy ib = new InstructionBusy(id);
		ib.setComments(ctx.getParameter("comments"));
		ib.setStartTime(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
		ib.setEndTime(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));

		// Get the DAO and write the entry
		try {
			SetAcademyCalendar wdao = new SetAcademyCalendar(ctx.getConnection());
			wdao.write(ib);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the busy time calendar
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("busycalendar", null, ctx.getUser().getID());
		result.setSuccess(true);
	}
}
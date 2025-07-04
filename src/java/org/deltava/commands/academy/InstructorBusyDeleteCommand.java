// Copyright 2007, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.time.Instant;

import org.deltava.beans.academy.InstructionBusy;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete an Instructor's busy time entry.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class InstructorBusyDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the instructor ID and start time
		int instructorID = ctx.getID();
		Instant sd = StringUtils.parseInstant(ctx.getParameter("op"), "MMddyyyyHHmm");
		
		// Validate our delete access
		BusyTimeAccessControl ac = new BusyTimeAccessControl(ctx, new InstructionBusy(instructorID));
		ac.validate();
		if (!ac.getCanDelete())
			throw securityException("Cannot delete Instructor busy time entry");

		// Get the DAO and delete the entry
		try {
			SetAcademyCalendar wdao = new SetAcademyCalendar(ctx.getConnection());
			wdao.deleteBusy(instructorID, sd);
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
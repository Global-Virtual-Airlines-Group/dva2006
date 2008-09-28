// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.Date;
import java.sql.Connection;

import org.deltava.beans.academy.InstructionBusy;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete an Instructor's busy time entry.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructorBusyDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the instructor ID and start time
		int instructorID = ctx.getID();
		Date sd = StringUtils.parseDate(ctx.getParameter("op"), "MMddyyyyHHmm");
		
		// Validate our delete access
		BusyTimeAccessControl ac = new BusyTimeAccessControl(ctx, new InstructionBusy(instructorID));
		ac.validate();
		if (!ac.getCanDelete())
			throw securityException("Cannot delete Instructor busy time entry");

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and delete the entry
			SetAcademyCalendar wdao = new SetAcademyCalendar(con);
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
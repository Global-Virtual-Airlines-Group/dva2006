// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.Date;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

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
		
		// Get the instructor ID and validate access
		int instructorID = ctx.getID();
		if ((instructorID != ctx.getUser().getID()) && !ctx.isUserInRole("AcademyAdmin"))
			throw securityException("Cannot delete Instructor busy time entry");
		
		// Get the start time
		Date sd = new Date(StringUtils.parse(ctx.getParameter("startTime"), 0) * 1000);

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
		result.setType(CommandResult.REDIRECT);
		result.setURL("busycalendar", null, ctx.getUser().getID());
		result.setSuccess(true);
	}
}
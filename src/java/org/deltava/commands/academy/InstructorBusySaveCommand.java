// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.InstructionBusy;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save a new busy time for a Flight Academy Instructor.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructorBusySaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Build the bean
		InstructionBusy ib = new InstructionBusy(ctx.getUser().getID());
		ib.setComments(ctx.getParameter("comments"));
		ib.setStartTime(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
		ib.setEndTime(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));

		try {
			Connection con = ctx.getConnection();
			
			// Get the dao and write the entry
			SetAcademyCalendar wdao = new SetAcademyCalendar(con);
			wdao.write(ib);
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
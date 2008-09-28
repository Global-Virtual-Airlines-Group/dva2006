// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.sql.Connection;

import org.deltava.beans.acars.DispatchScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchScheduleAccessControl;

/**
 * A Web Site Command to delete an ACARS Dispatcher schedule entry.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */
public class ServiceEntryDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the entry
			GetDispatchCalendar dao = new GetDispatchCalendar(con);
			DispatchScheduleEntry dse = dao.get(ctx.getID());
			if (dse == null)
				throw notFoundException("Invalid Dispatch Schedule entry - " + ctx.getID());
			
			// Validate our access
			DispatchScheduleAccessControl ac = new DispatchScheduleAccessControl(ctx, dse);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Dispatch Schedule entry");
			
			// Get the DAO and delete
			SetDispatchCalendar wdao = new SetDispatchCalendar(con);
			wdao.delete(dse.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the calendar
		CommandResult result = ctx.getResult();
		result.setURL("dspcalendar.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}
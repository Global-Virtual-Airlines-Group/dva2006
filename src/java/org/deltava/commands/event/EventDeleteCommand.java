// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.sql.Connection;

import org.deltava.beans.event.Event;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to delete an Online Event.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class EventDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Unknown Online Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Online Event");
			
			// Delete the event
			SetEvent wdao = new SetEvent(con);
			wdao.delete(e);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/eventUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}
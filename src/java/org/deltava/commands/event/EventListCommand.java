// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to display Online Events.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class EventListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext vc = initView(ctx);
		try {
			GetEvent dao = new GetEvent(ctx.getConnection());
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			vc.setResults(dao.getEvents());

			// Calculate our access to create new events
			EventAccessControl access = new EventAccessControl(ctx, null);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/eventList.jsp");
		result.setSuccess(true);
	}
}
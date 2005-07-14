// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetEvent;
import org.deltava.dao.DAOException;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to display Online Events.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetEvent dao = new GetEvent(con);
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());

			// Get the events
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
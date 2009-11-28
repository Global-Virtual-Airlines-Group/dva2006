// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Date;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to purge Route data.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class RoutePurgeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanDelete())
			throw securityException("Cannot purge Routes");

		// If we're purging oceanic routes, figure out a date to purge before
		Date purgeDate = StringUtils.parseDate(ctx.getParameter("purgeDate"), "MM/dd/yyyy");

		// Get the DAO and purge the routes
		try {
			SetOceanic dao = new SetOceanic(ctx.getConnection());
			int rowsDeleted = dao.purgeOceanic(purgeDate);
			ctx.setAttribute("rowsDeleted", Integer.valueOf(rowsDeleted), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("purgeOceanic", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/routeUpdate.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Date;
import java.sql.Connection;

import java.text.*;

import org.deltava.commands.*;

import org.deltava.dao.SetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

/**
 * A Web Site Command to purge Route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RoutePurgeCommand extends AbstractCommand {

	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we are purging domestic routes
		boolean isDomestic = "domestic".equals(ctx.getCmdParameter(Command.OPERATION, null));
		ctx.setAttribute("purgeOceanic", Boolean.valueOf(!isDomestic), REQUEST);
		ctx.setAttribute("purgeDomestic", Boolean.valueOf(isDomestic), REQUEST);

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanDelete())
			throw securityException("Cannot purge Routes");

		// If we're purging oceanic routes, figure out a date to purge before
		Date purgeDate = null;
		if (!isDomestic) {
			try {
				purgeDate = _df.parse(ctx.getParameter("purgeDate"));
			} catch (ParseException pe) {
				CommandException ce = new CommandException("Invalid Purge Date - " + ctx.getParameter("purgeDate"));
				ce.setLogStackDump(false);
				throw ce;
			}
		}

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and purge the routes
			SetRoute dao = new SetRoute(con);
			int rowsDeleted = (isDomestic) ? dao.purgeDomestic() : dao.purgeOceanic(purgeDate);
			ctx.setAttribute("rowsDeleted", new Integer(rowsDeleted), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/routeUpdate.jsp");
		result.setSuccess(true);
	}
}
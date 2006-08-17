// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Date;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete Route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RouteDeleteCommand extends AbstractCommand {

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
			throw securityException("Cannot delete Oceanic Route");
		
		// Get the date/type
		int routeType = StringUtils.parse((String) ctx.getCmdParameter(OPERATION, "0"), 0);
		Date vd = StringUtils.parseDate((String) ctx.getCmdParameter(ID, null), "MMddyyyy");

		try {
			Connection con = ctx.getConnection();

			// Get the Route - we don't care what it is (so long as it exists)
			GetRoute dao = new GetRoute(con);
			Object route = dao.get(routeType, vd);
			if (route == null)
				throw notFoundException("Invalid Oceanic Route - " + ctx.getID());

			// Get the DAO and delete the route
			SetRoute wdao = new SetRoute(con);
			wdao.deleteOceanic(routeType, vd);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status variable for the JSP
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/routeUpdate.jsp");
		result.setSuccess(true);
	}
}
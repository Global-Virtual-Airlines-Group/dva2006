// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetRoute;
import org.deltava.dao.SetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

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
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Route - we don't care what it is (so long as it exists)
			GetRoute dao = new GetRoute(con);
			Object route = dao.get(ctx.getID());
			if (route == null)
				throw new CommandException("Invalid Oceanic Route - " + ctx.getID());
			
			// Get the DAO and delete the route
			SetRoute wdao = new SetRoute(con);
			wdao.deleteOceanic(ctx.getID());
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
// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.sql.Connection;

import org.deltava.beans.acars.RoutePlan;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

/**
 * A Web Site Command to delete dispatcher routes.
 * @author Luke
 * @version 2.2
 * @since 2.1
 */

public class RouteDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the route
			GetACARSRoute rdao = new GetACARSRoute(con);
			RoutePlan rp = rdao.getRoute(ctx.getID());
			if (rp == null)
				throw notFoundException("Unknown Route ID - " + ctx.getID());
			
			// Check our access
			DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Dispatcher route");
			
			// Save in the request
			ctx.setAttribute("route", rp, REQUEST);
			
			// Delete the route
			SetACARSRoute wdao = new SetACARSRoute(con);
			wdao.delete(rp.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
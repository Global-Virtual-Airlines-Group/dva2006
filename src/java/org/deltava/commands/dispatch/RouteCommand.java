// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.RoutePlan;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchAccessControl;

/**
 * A Web Site Command to view saved dispatch routes.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class RouteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the route
			GetACARSRoute rdao = new GetACARSRoute(con);
			RoutePlan rp = rdao.getRoute(ctx.getID());
			if (rp == null)
				throw notFoundException("Unknown Route ID - " + ctx.getID());
			
			// Check our access
			DispatchAccessControl ac = new DispatchAccessControl(ctx);
			ac.validate();
			if (!ac.getCanView())
				throw securityException("Cannot view Dispatcher route");
			
			// Get the Author
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(rp.getAuthorID());
			Pilot p = pdao.get(ud);
			
			// Save in the request
			ctx.setAttribute("route", rp, REQUEST);
			ctx.setAttribute("author", p, REQUEST);
			ctx.setAttribute("authorLoc", ud, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeView.jsp");
		result.setSuccess(true);
	}
}
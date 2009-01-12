// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.sql.Connection;

import org.deltava.beans.acars.DispatchRoute;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

/**
 * A Web Site Command to enable or disable an ACARS Dispatch route.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class RouteToggleCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the route
			GetACARSRoute dao = new GetACARSRoute(con);
			DispatchRoute rt = dao.getRoute(ctx.getID());
			if (rt == null)
				throw notFoundException("Invalid Dispatch Route - " + ctx.getID());
			
			// Check our access
			DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx, rt);
			ac.validate();
			if (!ac.getCanDisable())
				throw securityException("Cannot disable Dispatch Route");
			
			// Disable the route
			SetACARSRoute wdao = new SetACARSRoute(con);
			wdao.activate(rt.getID(), !rt.getActive());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("dsproute", null, ctx.getID());
		result.setSuccess(true);
	}
}
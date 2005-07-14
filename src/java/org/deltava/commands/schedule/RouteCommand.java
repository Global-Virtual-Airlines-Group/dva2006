// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.RouteAccessControl;

/**
 * A Web Site Command to display Oceanic Route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
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
			
			// Get the DAO and the route
			GetRoute dao = new GetRoute(con);
			ctx.setAttribute("route", dao.get(ctx.getID()), REQUEST);
			
			// Get our access level
			RouteAccessControl access = new RouteAccessControl(ctx);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/oRoute.jsp");
		result.setSuccess(true);
	}
}
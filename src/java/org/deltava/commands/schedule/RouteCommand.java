// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Date;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

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
		
		// Get the date/type
		int routeType = StringUtils.parse((String) ctx.getCmdParameter(OPERATION, "0"), 0);
		Date vd = StringUtils.parseDate((String) ctx.getCmdParameter(ID, null), "MMddyyyy");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the route
			GetRoute dao = new GetRoute(con);
			ctx.setAttribute("route", dao.get(routeType, vd), REQUEST);
			
			// Get our access level
			ScheduleAccessControl access = new ScheduleAccessControl(ctx);
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
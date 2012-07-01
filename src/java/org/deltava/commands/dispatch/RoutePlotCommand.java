// Copyright 2008, 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.Collections;

import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a Dispatch route.
 * @author Luke
 * @version 4.2
 * @since 2.2
 */

public class RoutePlotCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check for dispatch route creation access
		DispatchRouteAccessControl access = new DispatchRouteAccessControl(ctx, null);
		access.validate();
		if (!access.getCanCreate())
			throw securityException("Cannot plot Dispatch routes");
		
		// Check for specified Airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		ctx.setAttribute("airportD", aD, REQUEST);
		ctx.setAttribute("airportA", aA, REQUEST);
		
		// Load the airlines
		if ((aD != null) && (aA != null)) {
			ctx.setAttribute("airportsD", Collections.singleton(aD), REQUEST);
			ctx.setAttribute("airportsA", Collections.singleton(aA), REQUEST);
			try {
				GetSchedule sdao = new GetSchedule(ctx.getConnection());
				ctx.setAttribute("airlines", sdao.getAirlines(aD, aA), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		} else
			ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routePlot.jsp");
		result.setSuccess(true);
	}
}
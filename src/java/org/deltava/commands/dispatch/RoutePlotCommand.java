// Copyright 2008, 2009, 2011, 2012, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a Dispatch route.
 * @author Luke
 * @version 10.6
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
				Connection con = ctx.getConnection();
				GetRawSchedule rsdao = new GetRawSchedule(con);
				GetSchedule sdao = new GetSchedule(con);
				sdao.setSources(rsdao.getSources(true, ctx.getDB()));
				ctx.setAttribute("airlines", sdao.getAirlines(RoutePair.of(aD, aA)), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		} else
			ctx.setAttribute("airlines", SystemData.getAirlines().stream().filter(Airline::getActive).collect(Collectors.toList()), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routePlot.jsp");
		result.setSuccess(true);
	}
}
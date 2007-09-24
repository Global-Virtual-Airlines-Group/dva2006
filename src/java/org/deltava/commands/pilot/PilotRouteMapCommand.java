// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to disply all the routes the pilot has flown.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 */

public class PilotRouteMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the routes
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<RoutePair> routes = frdao.getRoutePairs(ctx.getUser().getID());
			
			// Save the user's home airport
			Airport airportH = SystemData.getAirport(ctx.getUser().getHomeAirport());
			
			// Get the airports
			Collection<Airport> airports = new LinkedHashSet<Airport>();
			for (Iterator<RoutePair> i = routes.iterator(); i.hasNext(); ) {
				RoutePair rp = i.next();
				airports.add(rp.getAirportD());
				airports.add(rp.getAirportA());
			}
			
			// Save in request
			ctx.setAttribute("routes", routes, REQUEST);
			ctx.setAttribute("home", airportH, REQUEST);
			airports.remove(airportH);
			ctx.setAttribute("airports", airports, REQUEST);
			ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/myRouteMap.jsp");
		result.setSuccess(true);
	}
}
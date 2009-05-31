// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to disply all the routes the pilot has flown.
 * @author LKolin
 * @version 2.6
 * @since 1.0
 */

public class PilotRouteMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the pilot ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(userID);
			if (usr == null)
				throw notFoundException("Unknown Pilot ID - " + userID);
			
			// Get the routes
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<RoutePair> routes = frdao.getRoutePairs(userID);
			
			// Save the user's home airport
			Airport airportH = SystemData.getAirport(usr.getHomeAirport());
			
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
			ctx.setAttribute("pilot", usr, REQUEST);
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
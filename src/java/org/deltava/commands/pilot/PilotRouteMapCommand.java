// Copyright 2007, 2009, 2012, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import org.deltava.beans.Pilot;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to disply all the routes the pilot has flown.
 * @author Luke
 * @version 5.4
 * @since 1.0
 */

public class PilotRouteMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the pilot ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			Pilot usr = pdao.get(userID);
			if (usr == null)
				throw notFoundException("Unknown Pilot ID - " + userID);
			
			// Save the user's home airport
			Airport airportH = SystemData.getAirport(usr.getHomeAirport());
			if (airportH == null)
				airportH = SystemData.getAirport("LFPG");
			
			// Save in request
			ctx.setAttribute("home", airportH, REQUEST);
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
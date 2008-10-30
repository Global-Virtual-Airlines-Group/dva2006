// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a Dispatch route.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class RoutePlotCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Determine if the user uses IATA/ICAO codes
		Person usr = ctx.getUser();
		boolean useIATA = (usr == null) ? false : (usr.getAirportCodeType() == Airport.IATA);
		ctx.setAttribute("useIATA", Boolean.valueOf(useIATA), REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Check for dispatch route creation access
		DispatchRouteAccessControl access = new DispatchRouteAccessControl(ctx);
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
			try {
				GetSchedule sdao = new GetSchedule(ctx.getConnection());
				ctx.setAttribute("airlnes", sdao.getAirlines(aD, aA), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routePlot.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;

import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Airport;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot Terminal Routes.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class TerminalRoutePlotCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		try {
			GetAirport dao = new GetAirport(ctx.getConnection());
			airports.addAll(dao.getWithTerminalRoutes());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("airport"));
		if (a == null)
			a = SystemData.getAirport(ctx.getUser().getHomeAirport());
		
		// Save in request
		ctx.setAttribute("airports", airports, REQUEST);
		ctx.setAttribute("emptyList", Collections.emptyList(), REQUEST);
		ctx.setAttribute("trTypes", Arrays.asList(TerminalRoute.TYPES), REQUEST);
		ctx.setAttribute("mapCenter", a, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/sidstarPlot.jsp");
		result.setSuccess(true);
	}
}
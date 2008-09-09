// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class RoutePlotCommand extends AbstractCommand {
	
	private static final List SIM_VERSIONS = ComboUtils.fromArray(new String[] {"Flight Simulator X", "Flight Simulator 2004"},
			new String[] {"FSX", "FS2004"}); 

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
		ctx.setAttribute("simVersions", SIM_VERSIONS, REQUEST);
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Check for dispatch route creation access
		DispatchRouteAccessControl access = new DispatchRouteAccessControl(ctx);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Check for specified Airports
		if (access.getCanCreate()) {
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
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}
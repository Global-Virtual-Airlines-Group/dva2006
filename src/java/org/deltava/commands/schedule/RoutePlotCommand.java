// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;

import org.deltava.util.ComboUtils;

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

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}
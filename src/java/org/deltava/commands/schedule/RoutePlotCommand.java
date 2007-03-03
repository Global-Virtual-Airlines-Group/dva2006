// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 1.0
 * @since 1.0
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

		// Sort and save the airports, and force ICAO codes
		Collection<ComboAlias> apSet = new TreeSet<ComboAlias>();
		Collection<Airport> airports = new HashSet<Airport>(SystemData.getAirports().values());
		for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
			Airport a = i.next();
			String code = useIATA ? a.getIATA() : a.getICAO();
			apSet.add(ComboUtils.fromString(a.getName() + " (" + code + ")", code));
		}
		
		// Save the airports in the request
		ctx.setAttribute("airports", apSet, REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}
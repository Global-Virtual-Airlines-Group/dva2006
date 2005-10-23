// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

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

		// Generate empty list for JSP
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);

		// Sort and save the airports, and force ICAO codes
		Map airports = (Map) SystemData.getObject("airports");
		Set apSet = new TreeSet();
		for (Iterator i = airports.values().iterator(); i.hasNext(); ) {
			Airport a = (Airport) i.next();
			apSet.add(ComboUtils.fromString(a.getName() + " (" + a.getICAO() + ")", a.getICAO()));
		}
		
		// save the airports in the request
		ctx.setAttribute("airports", apSet, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}
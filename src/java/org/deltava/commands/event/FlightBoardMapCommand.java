// Copyright 2005, 2006, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import org.deltava.beans.OnlineNetwork;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the "who is online" page.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class FlightBoardMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the network name and whether we display a map
		OnlineNetwork network = OnlineNetwork.valueOf(SystemData.get("online.default_network"));
		try {
			network = OnlineNetwork.valueOf(ctx.getParameter("id").toUpperCase());
		} catch (Exception e) {
			// empty
		}

		// Load the network names and save in the request
		ctx.setAttribute("networks", SystemData.getObject("online.networks"), REQUEST);
		ctx.setAttribute("network", network, REQUEST);

		// Forward to the display JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/flightBoardMap.jsp");
		result.setSuccess(true);
	}
}
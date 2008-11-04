// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to centralize weather information. 
 * @author Luke
 * @version 2.3
 * @since 2.2
 */

public class WeatherCenterCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot's home airport
		ctx.setAttribute("homeAirport", SystemData.getAirport(ctx.getUser().getHomeAirport()), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/wxCenter.jsp");
		result.setSuccess(true);
	}
}
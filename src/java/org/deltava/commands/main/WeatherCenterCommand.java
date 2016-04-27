// Copyright 2008, 2009, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to centralize weather information. 
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class WeatherCenterCommand extends AbstractCommand {
	
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the pilot's home airport
		ctx.setAttribute("homeAirport", SystemData.getAirport(ctx.getUser().getHomeAirport()), REQUEST);
		
		try {
			GetMetadata mddao = new GetMetadata(ctx.getConnection());
			ctx.setAttribute("gfsCycle", mddao.getDate("gfs.cycle"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/wxCenter.jsp");
		result.setSuccess(true);
	}
}
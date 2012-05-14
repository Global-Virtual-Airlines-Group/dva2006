// Copyright 2005, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class RoutePlotCommand extends AbstractCommand {
	
	private static final List<?> SIM_VERSIONS = ComboUtils.fromArray(new String[] {"Flight Simulator X", 
			"Flight Simulator 2004", "X-Plane 9"}, new String[] {"FSX", "FS9", "XP9"}); 

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Set request attributes
		ctx.setAttribute("emptyList", Collections.emptyList(), REQUEST);
		ctx.setAttribute("simVersions", SIM_VERSIONS, REQUEST);
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		try {
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(SystemData.get("airline.code")), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}
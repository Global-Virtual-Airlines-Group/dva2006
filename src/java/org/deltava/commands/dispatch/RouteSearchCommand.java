// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search saved Dispatch routes. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class RouteSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Load airports
			GetACARSRoute dao = new GetACARSRoute(con);
			ctx.setAttribute("airport", dao.getAirports(), REQUEST);
			
			// If no airport specified, go to the JSP
			if (ctx.getParameter("airportD") == null) {
				ctx.release();
				
				CommandResult result = ctx.getResult();
				result.setURL("/jsp/dispatch/routeSearch.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the airports and do the search
			Airport airportD = SystemData.getAirport(ctx.getParameter("airportD"));
			Airport airportA = SystemData.getAirport(ctx.getParameter("airportA"));
			ctx.setAttribute("results", dao.getRoutes(airportD, airportA), REQUEST);
			
			// Load Author IDs
			Collection<Integer> IDs = dao.getAuthorIDs();
			
			// Load Authors
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeSearch.jsp");
		result.setSuccess(true);
	}
}
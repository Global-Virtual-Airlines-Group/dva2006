// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search saved Dispatch routes. 
 * @author Luke
 * @version 2.5
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
			Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			GetACARSRoute dao = new GetACARSRoute(con);
			airports.addAll(dao.getAirports());
			ctx.setAttribute("airports", airports, REQUEST);
			
			// If airports specified, do the search
			if (ctx.getParameter("airportD") != null) {
				Airport airportD = SystemData.getAirport(ctx.getParameter("airportD"));
				Airport airportA = SystemData.getAirport(ctx.getParameter("airportA"));
				ctx.setAttribute("airportD", airportD, REQUEST);
				ctx.setAttribute("airportA", airportA, REQUEST);
				
				// Do the search
				Collection<Integer> IDs = new HashSet<Integer>();
				Collection<DispatchRoute> results = dao.getRoutes(airportD, airportA, false);
				for (Iterator<DispatchRoute> i = results.iterator(); i.hasNext(); ) {
					DispatchRoute rt = i.next();
					DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx, rt);
					ac.validate();
					if (ac.getCanView())
						IDs.add(new Integer(rt.getAuthorID()));
					else
						i.remove();
				}

				// Load Authors
				GetPilot pdao = new GetPilot(con);
				GetUserData uddao = new GetUserData(con);
				UserDataMap udm = uddao.get(IDs);

				// Set search attribute and authors
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
				ctx.setAttribute("authors", pdao.get(udm), REQUEST);
				ctx.setAttribute("results", results, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate access
		DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeSearch.jsp");
		result.setSuccess(true);
	}
}
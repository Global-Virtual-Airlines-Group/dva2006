// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.RoutePlan;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

/**
 * A Web Site Command to view saved dispatch routes.
 * @author Luke
 * @version 2.2
 * @since 2.1
 */

public class RouteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the route
			GetACARSRoute rdao = new GetACARSRoute(con);
			RoutePlan rp = rdao.getRoute(ctx.getID());
			if (rp == null)
				throw notFoundException("Unknown Route ID - " + ctx.getID());
			
			// Check our access
			DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx);
			ac.validate();
			if (!ac.getCanView())
				throw securityException("Cannot view Dispatcher route");
			
			// Get the Author
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(rp.getAuthorID());
			Pilot p = pdao.get(ud);
			
			// Calculate the mid-point and distance
			ctx.setAttribute("distance", new Integer(rp.getAirportD().getPosition().distanceTo(rp.getAirportA())), REQUEST);
			ctx.setAttribute("mapCenter", rp.getAirportD().getPosition().midPoint(rp.getAirportA()), REQUEST);
			
			// Save the waypoints including the airports
			Collection<MapEntry> wpts = new LinkedHashSet<MapEntry>();
			wpts.add(rp.getAirportD());
			wpts.addAll(rp.getWaypoints());
			wpts.add(rp.getAirportA());
			
			// Save in the request
			ctx.setAttribute("route", rp, REQUEST);
			ctx.setAttribute("waypoints", wpts, REQUEST);
			ctx.setAttribute("author", p, REQUEST);
			ctx.setAttribute("authorLoc", ud, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeView.jsp");
		result.setSuccess(true);
	}
}
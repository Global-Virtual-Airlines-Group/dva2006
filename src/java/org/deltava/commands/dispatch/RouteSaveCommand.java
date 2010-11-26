// Copyright 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to create a new ACARS Dispatcher route.
 * @author Luke
 * @version 3.4
 * @since 2.2
 */

public class RouteSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airports
		DispatchRoute rp = new DispatchRoute();
		rp.setAuthorID(ctx.getUser().getID());
		rp.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
		rp.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		rp.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		rp.setAirportL(SystemData.getAirport(ctx.getParameter("airportL")));
		rp.setCruiseAltitude(ctx.getParameter("cruiseAlt"));
		rp.setRoute(ctx.getParameter("route"));
		rp.setComments(ctx.getParameter("comments"));
		
		// Update the route ID
		int routeID = StringUtils.parse(ctx.getParameter("routeID"), 0);
		if (routeID > 0) {
			rp.setID(routeID);
			rp.setActive(true);
		}
		
		List<String> wpCodes = StringUtils.split(ctx.getParameter("route"), " ");
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			
			// Check if we have a SID - fix wonkiness if terminating the SID early 
			TerminalRoute sid = dao.getRoute(rp.getAirportD(), TerminalRoute.SID, ctx.getParameter("sid"), true);
			if (sid != null) {
				rp.setSID(sid.getCode());
				String transition = wpCodes.isEmpty() ? sid.getTransition() : wpCodes.get(0);
				for (NavigationDataBean nd : sid.getWaypoints(transition))
					rp.addWaypoint(nd, sid.getCode());
			}
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(ctx.getParameter("route"), rp.getAirportD());
				for (NavigationDataBean nd : points)
					rp.addWaypoint(nd, nd.getAirway());
			}
			
			// Check if we have a STAR - fix wonkiness if joining the STAR late
			TerminalRoute star = dao.getRoute(rp.getAirportA(), TerminalRoute.STAR, ctx.getParameter("star"), true);
			if (star != null) {
				rp.setSTAR(star.getCode());
				String transition = wpCodes.isEmpty() ? star.getTransition() : wpCodes.get(wpCodes.size() - 1);
				for (NavigationDataBean nd : star.getWaypoints(transition))
					rp.addWaypoint(nd, star.getCode());
			}
			
			// Check for a duplicate
			GetACARSRoute rdao = new GetACARSRoute(con);
			int dupeID = rdao.hasDuplicate(rp.getAirportD(), rp.getAirportA(), rp.getRoute());
			ctx.setAttribute("dupeID", Integer.valueOf(dupeID), REQUEST);
			
			// Save the route
			if ((dupeID == 0) || (dupeID == rp.getID())) {
				SetACARSRoute wdao = new SetACARSRoute(con);
				wdao.write(rp);
				ctx.setAttribute("isCreate", Boolean.valueOf(rp.getID() == 0), REQUEST);
				ctx.setAttribute("isUpdate", Boolean.valueOf(rp.getID() != 0), REQUEST);
			} else
				ctx.setAttribute("isDupe", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("route", rp, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
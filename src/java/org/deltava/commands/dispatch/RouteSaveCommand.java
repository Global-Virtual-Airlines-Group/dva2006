// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.RoutePlan;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to create a new ACARS Dispatcher route.
 * @author Luke
 * @version 2.2
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
		RoutePlan rp = new RoutePlan();
		rp.setAuthorID(ctx.getUser().getID());
		rp.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
		rp.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		rp.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		rp.setAirportL(SystemData.getAirport(ctx.getParameter("airportL")));
		rp.setCruiseAltitude(ctx.getParameter("cruiseAlt"));
		rp.setRoute(ctx.getParameter("route"));
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			
			// Check if we have a SID
			if (!StringUtils.isEmpty(ctx.getParameter("sid"))) {
				TerminalRoute sid = dao.getRoute(ctx.getParameter("sid"));
				if (sid != null) {
					rp.setSID(sid.getCode());
					for (NavigationDataBean nd : sid.getWaypoints())
						rp.addWaypoint(nd, sid.getCode());
				}
			}
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(ctx.getParameter("route"), rp.getAirportD());
				for (NavigationDataBean nd : points)
					rp.addWaypoint(nd, nd.getAirway());
			}
			
			// Check if we have a STAR
			if (!StringUtils.isEmpty(ctx.getParameter("star"))) {
				TerminalRoute star = dao.getRoute(ctx.getParameter("star"));
				if (star != null) {
					rp.setSTAR(star.getCode());
					for (NavigationDataBean nd : star.getWaypoints())
						rp.addWaypoint(nd, star.getCode());
				}
			}
			
			// Save the route
			SetACARSRoute wdao = new SetACARSRoute(con);
			wdao.write(rp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("route", rp, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
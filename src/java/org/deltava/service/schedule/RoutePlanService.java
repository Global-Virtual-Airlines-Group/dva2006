// Copyright 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.flightplan.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create flight plans. 
 * @author Luke
 * @version 3.6
 * @since 2.2
 */

public class RoutePlanService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airports and altitude
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		String alt = ctx.getParameter("cruiseAlt");
		if (!StringUtils.isEmpty(alt) && (alt.startsWith("FL")))
			alt = alt.substring(2) + "00";
		if (StringUtils.parse(alt, -1) < 1000)
			alt = "35000";
		
		// Validate the airports
		if (aD == null)
			throw error(SC_BAD_REQUEST, "Invalid Departure Airport - " + ctx.getParameter("airportD"), false);
		else if (aA == null)
			throw error(SC_BAD_REQUEST, "Invalid Arrival Airport - " + ctx.getParameter("airportA"), false);
		
		// Get the Flight Plan generator
		FlightPlanGenerator fpgen = null;
		if ("FSX".equals(ctx.getParameter("simVersion")))
			fpgen = new FS9Generator();
		else if ("XP9".equals(ctx.getParameter("simVersion")))
			fpgen = new XP9Generator();
		else
			fpgen = new FS9Generator();
		
		// Update the flight plan
		fpgen.setAirports(aD, aA);
		fpgen.setCruiseAltitude(alt);
		
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		routePoints.add(new AirportLocation(aD));
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			
			// Load the SID
			TerminalRoute sid = dao.getRoute(aD, TerminalRoute.SID, ctx.getParameter("sid"));
			if (sid != null) {
				routePoints.addAll(sid.getWaypoints());
				fpgen.setSID(sid);
			}
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(ctx.getParameter("route"), aD);
				routePoints.addAll(points);
			}
			
			// Load the STAR
			TerminalRoute star = dao.getRoute(aA, TerminalRoute.STAR, ctx.getParameter("star"));
			if (star != null) {
				routePoints.addAll(star.getWaypoints());
				fpgen.setSTAR(star);
			}
			
			// Add the destination airport
			routePoints.add(new AirportLocation(aA));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Flush the output buffer
		try {
			ctx.getResponse().setContentType(fpgen.getMimeType());
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + aD.getICAO() +"-" + aA.getICAO() + "." + fpgen.getExtension());
			ctx.getResponse().getOutputStream().write(fpgen.generate(routePoints));
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}
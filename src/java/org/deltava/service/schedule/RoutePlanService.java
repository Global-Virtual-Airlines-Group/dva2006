// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create Microsoft Flight Simulator flight plans. 
 * @author Luke
 * @version 2.3
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
		if (StringUtils.parse(alt, -1) < 1000)
			alt = "35000";
		
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		routePoints.add(new AirportLocation(aD));
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			
			// Load the SID
			TerminalRoute sid = dao.getRoute(ctx.getParameter("sid"));
			if (sid != null)
				routePoints.addAll(sid.getWaypoints());
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(ctx.getParameter("route"), aD);
				routePoints.addAll(points);
			}
			
			// Load the STAR
			TerminalRoute star = dao.getRoute(ctx.getParameter("star"));
			if (star != null)
				routePoints.addAll(star.getWaypoints());
			
			// Add the destination airport
			routePoints.add(new AirportLocation(aA));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Write the flight plan
		boolean isFSX = "FSX".equals(ctx.getParameter("simVersion"));
		ctx.println("[flightplan]");
		if (!isFSX)
			ctx.println("AppVersion=9.0.30612");
		ctx.println("title=" + aD.getICAO() + " to " + aA.getICAO());
		ctx.println("description=" + aD.getICAO() + ", " + aA.getICAO());
		ctx.println("type=IFR");
		ctx.println("routetype=0");
		ctx.println("cruising_altitude=" + alt);
		if (isFSX) {
			ctx.println("departure_id=" + aD.getICAO() + ", " + GeoUtils.formatFS9(aD) + ", +" + StringUtils.format(aD.getAltitude(), "000000.00") + ",");
			ctx.println("destination_id=" + aA.getICAO() + ", " + GeoUtils.formatFS9(aA) + ", " + StringUtils.format(aA.getAltitude(), "000000.00") + ",");
			ctx.println("departure_name=" + aD.getName());
			ctx.println("destination_name=" + aA.getName());
		} else {
			ctx.println("departure_id=" + aD.getICAO() + ", " + GeoUtils.formatFS9(aD) + ", +000000.00,");
			ctx.println("departure_name=" + aD.getName());
			ctx.println("departure_position=GATE ?");
			ctx.println("destination_id=" + aA.getICAO() + ", " + GeoUtils.formatFS9(aA) + ", +000000.00,");
			ctx.println("destination_name=" + aA.getName());
		}

		// Write the route entries
		int waypointIdx = 0;
		for (Iterator<NavigationDataBean> i = routePoints.iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			ctx.print("waypoint." + String.valueOf(waypointIdx) + "=");
			if (isFSX) {
				ctx.print(nd.getCode());
				switch (nd.getType()) {
					case NavigationDataBean.AIRPORT:
						ctx.print(", A, ");
						break;
					
					case NavigationDataBean.NDB:
					ctx.print(", N, ");
						break;
					
					case NavigationDataBean.VOR:
					ctx.print(", V, ");
						break;
			
					default:
						ctx.print(", I, ");
				}
				
				ctx.print(GeoUtils.formatFS9(nd));
				ctx.print(", +000000.00,");
				if (nd.isInTerminalRoute()) {
					String aw = nd.getAirway();
					ctx.print(" ");
					ctx.println(aw.substring(0, aw.indexOf('.')));
				} else if (nd.getAirway() != null) {
					ctx.print(" ");
					ctx.println(nd.getAirway());
				} else
					ctx.println("");
			} else {
				if (nd.getRegion() != null)
					ctx.print(nd.getRegion());
				ctx.print(", ");
				ctx.print(nd.getCode());
				ctx.print(", ,");
				ctx.print(nd.getCode());
				switch (nd.getType()) {
					case NavigationDataBean.AIRPORT:
						ctx.print(", A, ");
						break;
						
					case NavigationDataBean.NDB:
						ctx.print(", N, ");
						break;
						
					case NavigationDataBean.VOR:
						ctx.print(", V, ");
						break;
				
					default:
						ctx.print(", I, ");
				}
				
				ctx.print(GeoUtils.formatFS9(nd));
				ctx.println(", +000000.00,");
			}
			
			waypointIdx++;
		}
		
		// Flush the output buffer
		try {
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + aD.getICAO() +"-" + aA.getICAO() + ".pln");
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}
// Copyright 2008, 2009, 2010, 2011, 2012, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the available Dispatch Routes between two Airports.
 * @author Luke
 * @version 9.0
 * @since 2.2
 */

public class DispatchRouteListService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse request data
		JSONObject req = null;
		try {
			req = new JSONObject(new JSONTokener(ctx.getRequest().getInputStream()));
		} catch (Exception e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		}
		
		// Get the airports
		Airport aD = SystemData.getAirport(req.optString("airportD"));
		Airport aA = SystemData.getAirport(req.optString("airportA"));
		
		// Check if loading from FlightAware
		boolean doFA = req.optBoolean("external") && SystemData.getBoolean("schedule.flightaware.enabled");
		boolean hasFARole = ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch") || ctx.isUserInRole("Operations");
		boolean doRoute = req.optBoolean("fullRoute");
		boolean forceFAReload = hasFARole && req.optBoolean("faReload");
		
		// Check for default runway
		String rwy = req.optString("runway");
		if ((rwy != null) && !rwy.startsWith("RW"))
			rwy = null;
		
		// Get the Data
		Collection<FlightRoute> routes = new ArrayList<FlightRoute>();
		try {
			RouteLoadHelper helper = new RouteLoadHelper(ctx.getConnection(), new ScheduleRoute(aD, aA));
			helper.setPreferredRunway(rwy);
			helper.loadDispatchRoutes();
			
			// Load cached routes
			helper.loadCachedRoutes();
			
			// Load flight aware routes
			if (forceFAReload || (doFA && hasFARole && !helper.hasRoutes())) {
				APILogger.add(new APIRequest(API.FlightAware.createName("ROUTES"), !ctx.isAuthenticated()));
				helper.loadFlightAwareRoutes(true);
			}
			
			// Load PIREP routes
			if (!helper.hasRoutes())
				helper.loadPIREPRoutes();
			
			// Get the weather
			helper.loadWeather();
			
			// Fix the SID/STAR
			helper.calculateBestTerminalRoute();
			routes.addAll(helper.getRoutes());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} finally {
			ctx.release();
		}
		
		// Create the JSON document
		JSONObject jo = new JSONObject();
		jo.put("airportD", aD.getICAO());
		jo.put("airportA", aA.getICAO());
		
		// Save the routes, stripping out duplicates
		Collection<String> rts = new HashSet<String>();
		for (FlightRoute rt : routes) {
			if (!rts.add(rt.getRoute())) continue;
			
			boolean isExternal = (rt instanceof ExternalFlightRoute); 
			StringBuilder buf = new StringBuilder();
			JSONObject ro = new JSONObject();
			ro.put("altitude", rt.getCruiseAltitude());
			ro.put("external", isExternal);
			ro.put("waypoints", rt.getRoute());
			ro.put("comments", rt.getComments());
			if (rt.getSID() != null) {
				String sid = rt.getSID();
				if (sid.endsWith(".ALL") && (rwy != null))
					sid = sid.replace("ALL", rwy);
				
				ro.put("sid", sid);
			}
				
			if (rt.getSTAR() != null)
				ro.put("star", rt.getSTAR());
			if (rt instanceof DispatchRoute) {
				ro.put("id", String.valueOf(rt.getID()));
				buf.append('#');
				buf.append(String.valueOf(rt.getID()));
			} else if (isExternal) {
				ro.put("id", "EXT" + String.valueOf(rt.getID()));
				buf.append("EXT");
				buf.append(String.valueOf(rt.getID()));
			}
			
			// Build the label
			buf.append(" - ");
			List<String> wps = StringUtils.split(rt.toString(), " ");
			if ((wps.size() > 10) && !doRoute && !isExternal) {
				buf.append(StringUtils.listConcat(wps.subList(0, 3), " "));
				buf.append(" ... ");
				buf.append(StringUtils.listConcat(wps.subList(wps.size() - 2, wps.size()), " "));
			} else
				buf.append(rt.toString());

			// Add the source
			if (isExternal) {
				buf.append(" (");
				buf.append(((ExternalFlightRoute) rt).getSource());
				buf.append(')');
			}
			
			// Add the element
			ro.put("name", buf.toString());
			jo.append("routes", ro);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "routes");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}
}
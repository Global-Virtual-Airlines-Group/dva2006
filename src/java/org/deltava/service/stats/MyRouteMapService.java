// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RouteStats;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display a Pilot's Flight Report routes to a Google map.
 * @author Luke
 * @version 5.4
 * @since 5.4
 */

public class MyRouteMapService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;

		// Get the routes
		List<RouteStats> routes = new ArrayList<RouteStats>();
		try {
			GetFlightReports frdao = new GetFlightReports(ctx.getConnection());
			routes.addAll(frdao.getRoutePairs(userID));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Get the airports
		Airport.Code ac = ctx.getUser().getAirportCodeType();
		Collection<Airport> airports = new LinkedHashSet<Airport>();
		for (RoutePair rp : routes) {
			airports.add(rp.getAirportD());
			airports.add(rp.getAirportA());
		}
		
		// Create the response
		Collections.sort(routes, Collections.reverseOrder());
		int max = Math.max(1, routes.get(0).getFlights());
		JSONObject jo = new JSONObject();
		try {
			// Add airports
			JSONArray aa = new JSONArray();
			for (Airport a : airports) {
				JSONObject ao = new JSONObject();
				ao.put("ll", GeoUtils.toJSON(a));
				ao.put("icao", a.getICAO());
				ao.put("code", (ac == Airport.Code.ICAO) ? a.getICAO() : a.getIATA());
				ao.put("name", a.getName());
				ao.put("desc", a.getInfoBox());
				aa.put(ao);
			}
		
			// Add route data
			JSONArray ra = new JSONArray();
			for (RouteStats r : routes) {
				JSONObject ro = new JSONObject();
				ro.put("ll", GeoUtils.toJSON(r));
				ro.put("desc", r.getInfoBox());
				ro.put("ratio", Math.max(1, r.getFlights() * 100 / max));
				JSONArray pa = new JSONArray();
				for (GeoLocation loc : r.getPoints())
					pa.put(GeoUtils.toJSON(loc));
				
				ro.put("src", r.getAirportD().getICAO());
				ro.put("dst", r.getAirportA().getICAO());
				ro.put("points", pa);
				ra.put(ro);
			}
			
			// Aggregate
			jo.put("airports", aa);
			jo.put("routes", ra);
		} catch (JSONException je) {
			throw error(SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(900);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
	
	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE always
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}
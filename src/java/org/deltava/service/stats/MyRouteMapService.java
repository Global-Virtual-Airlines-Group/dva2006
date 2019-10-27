// Copyright 2014, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;

import org.json.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RouteStats;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display a Pilot's Flight Report routes to a Google map.
 * @author Luke
 * @version 8.7
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
		
		// Get the date range
		int days = StringUtils.parse(ctx.getParameter("days"), 0);

		// Get the routes and the earliest flight
		List<RouteStats> routes = new ArrayList<RouteStats>();
		try {
			GetFlightReports frdao = new GetFlightReports(ctx.getConnection());
			routes.addAll(frdao.getRoutePairs(userID, days));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Get the airports
		Collection<Airport> airports = routes.stream().flatMap(rp -> List.of(rp.getAirportD(), rp.getAirportA()).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
		
		// Create the response
		Collections.sort(routes, Collections.reverseOrder());
		int max = routes.isEmpty() ? 0 : Math.max(1, routes.get(0).getFlights());
		JSONObject jo = new JSONObject();

		// Add airports
		for (Airport a : airports) {
			JSONObject ao = new JSONObject();
			ao.put("ll", JSONUtils.format(a));
			ao.put("icao", a.getICAO());
			ao.put("code", (ctx.getUser().getAirportCodeType() == Airport.Code.ICAO) ? a.getICAO() : a.getIATA());
			ao.put("name", a.getName());
			ao.put("desc", a.getInfoBox());
			jo.append("airports", ao);
		}
		
		// Add route data
		for (RouteStats r : routes) {
			JSONObject ro = new JSONObject();
			ro.put("ll", JSONUtils.format(r));
			ro.put("desc", r.getInfoBox());
			ro.put("ratio", Math.max(1, r.getFlights() * 100 / max));
			ro.put("src", r.getAirportD().getICAO());
			ro.put("dst", r.getAirportA().getICAO());
			r.getPoints().forEach(loc -> ro.append("points", JSONUtils.format(loc)));
			jo.append("routes", ro);
		}
			
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "airports", "routes");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
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
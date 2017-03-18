// Copyright 2006, 2007, 2008, 2009, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display scheduled routes out of a particular Airport. 
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class RouteService extends WebService {
	
	private static final Map<String, String> LCOLORS = CollectionUtils.createMap(Arrays.asList(MapEntry.COLORS), Arrays.asList(MapEntry.LINECOLORS));
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("icao"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Unknown Airport - " + ctx.getParameter("icao"), false);
		
		Collection<ScheduleEntry> flights = null;
		try {
			GetSchedule dao = new GetSchedule(ctx.getConnection());
			flights = dao.getFlights(a, SystemData.getAirline(ctx.getParameter("airline")));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("icao", a.getICAO());

		// Create the routes
		Collection<Airport> dstAirports = new TreeSet<Airport>();
		for (ScheduleEntry entry : flights) {
			Airport ap = entry.getAirportA();
			if (dstAirports.contains(ap))
				continue;

			JSONObject ro = new JSONObject();
			ro.put("from", a.getICAO());
			ro.put("to", ap.getICAO());
			ro.put("airline", entry.getAirline().getCode());
			ro.put("color", LCOLORS.get(entry.getAirline().getColor()));
			ro.append("positions", JSONUtils.format(a));
			ro.append("positions", JSONUtils.format(ap));

			// Add to the root element
			jo.append("routes", ro);
			dstAirports.add(ap);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "routes");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}
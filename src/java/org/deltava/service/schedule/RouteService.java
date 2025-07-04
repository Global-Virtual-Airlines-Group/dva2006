// Copyright 2006, 2007, 2008, 2009, 2012, 2016, 2017, 2020, 2021, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display scheduled routes out of a particular Airport. 
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class RouteService extends WebService {
	
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
		Airline al = SystemData.getAirline(ctx.getParameter("airline"));
		try {
			Connection con = ctx.getConnection();
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetSchedule dao = new GetSchedule(con);
			dao.setSources(rsdao.getSources(true, ctx.getDB()));
			flights = dao.getFlights(a, al);
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
			if (dstAirports.contains(ap)) continue;

			JSONObject ro = new JSONObject();
			ro.put("from", a.getICAO());
			ro.put("to", ap.getICAO());
			ro.put("airline", entry.getAirline().getCode());
			ro.put("color", al.getColor());
			
			// Calculate GC route
			Collection<GeoLocation> positions = GeoUtils.greatCircle(a, ap, 100);
			positions.forEach(loc -> ro.append("positions", JSONUtils.format(loc)));

			// Add to the root element
			jo.append("routes", ro);
			dstAirports.add(ap);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "routes");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}
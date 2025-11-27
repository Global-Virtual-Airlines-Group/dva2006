// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.json.JSONObject;

import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return Airport statistics. This is a separate service to minimize page load time as many queries 
 * called from this Service can be expensive and time consuming.
 * @author Luke
 * @version 12.1
 * @since 12.1
 */

public class AirportInformationService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the Airport
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		if (a == null)
			return SC_NOT_FOUND;
		
		List<Airport> alternates = new ArrayList<Airport>();
		List<Airport> dgAirports = new ArrayList<Airport>();
		try {
			Connection con = ctx.getConnection();
			
			// Load populaer alternates
			GetACARSAlternate aadao = new GetACARSAlternate(con);
			aadao.setQueryMax(5);
			alternates.addAll(aadao.getAlternates(a));
			
			// Load Gate Airports
			GetGates gdao = new GetGates(con);
			dgAirports.addAll(gdao.getUsagePairs(a, true));
			dgAirports.sort(new AirportComparator(AirportComparator.NAME));
		} catch(DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the JSON object
		JSONObject jo = new JSONObject();
		jo.put("airport", JSONUtils.format(a));
		
		// Add the departure gate airports
		dgAirports.stream().map(JSONUtils::format).forEach(ao -> jo.accumulate("dgAirports", ao));
		
		// Add alternates and distance
		for (Airport aa : alternates) {
			JSONObject ao = new JSONObject();
			ao.put("airport", JSONUtils.format(aa));
			ao.put("distance", a.distanceTo(aa));
			jo.accumulate("alternates", ao);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "alternates", "dgAirports");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(1800);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
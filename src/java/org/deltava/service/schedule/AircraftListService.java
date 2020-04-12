// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to display aircraft profile information.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class AircraftListService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		Collection<Aircraft> results = new ArrayList<Aircraft>();
		try {
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			results.addAll(acdao.getAircraftTypes());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Generate the JSON document
		JSONArray ja = new JSONArray();
		for (Aircraft a : results) {
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			JSONObject ao = new JSONObject();
			ao.put("name", a.getName());
			ao.put("fullName", a.getFullName());
			ao.put("engines", a.getEngines());
			ao.put("historic", a.getHistoric());
			ao.put("etops", opts.getETOPS().name());
			ao.put("softRunway", opts.getUseSoftRunways());
			ao.put("range", opts.getRange());
			ao.put("seats", opts.getSeats());
			ao.put("cruiseSpeed", a.getCruiseSpeed());
			ao.put("baseFuel", a.getBaseFuel());
			ao.put("taxiFuel", a.getTaxiFuel());
			ao.put("fuelFlow", a.getFuelFlow());
			ao.put("maxWeight", a.getMaxWeight());
			ao.put("maxZFW", a.getMaxZeroFuelWeight());
			ao.put("maxTakeoffWeight", a.getMaxTakeoffWeight());
			ao.put("maxLandingWeight", a.getMaxLandingWeight());
			ao.put("minTakeoffRunway", opts.getTakeoffRunwayLength());
			ao.put("minLandingRunway", opts.getLandingRunwayLength());
			ja.put(ao);
		}

		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(600);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
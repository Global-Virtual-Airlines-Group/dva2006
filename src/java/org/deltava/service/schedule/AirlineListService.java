// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.schedule.Airline;

import org.deltava.service.*;

import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a list of active Airlines. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class AirlineListService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the airlines
		Collection<Airline> airlines = new TreeSet<Airline>(SystemData.getAirlines().values());
		
		// Convert to JSON
		JSONArray ja = new JSONArray();
		for (Airline a : airlines) {
			JSONObject ao = new JSONObject();
			ao.put("code", a.getCode());
			ao.put("name", a.getName());
			ao.put("active", a.getActive());
			ao.put("historic", a.getHistoric());
			a.getCodes().forEach(c -> ao.append("codes", c));
			JSONUtils.ensureArrayPresent(ao, "codes");
			ja.put(ao);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
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
}
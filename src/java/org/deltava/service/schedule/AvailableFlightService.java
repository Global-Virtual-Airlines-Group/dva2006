// Copyright 2006, 2007, 2008, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return the next available Flight Number in the Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AvailableFlightService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get start/end ranges
		int startFlight = StringUtils.parse(ctx.getParameter("start"), 0);
		int endFlight = StringUtils.parse(ctx.getParameter("end"), 0);
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		if (a == null)
			a = SystemData.getAirline(SystemData.get("airline.code"));
		
		Collection<Integer> flights = null;
		try {
			GetRawScheduleInfo dao = new GetRawScheduleInfo(ctx.getConnection());
			flights = dao.getFlightNumbers(a, startFlight, endFlight);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Find the first available flight number in the range
		int flightNumber = startFlight + 1;
		for (Integer fn : flights) {
			if (flightNumber < fn.intValue()) break;
			flightNumber++;
		}
			
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("airline", a.getCode());
		jo.put("number", flightNumber);
		jo.put("leg", 1);
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}
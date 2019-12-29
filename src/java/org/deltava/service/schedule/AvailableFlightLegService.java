// Copyright 2006, 2007, 2008, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return the next available Leg number for a Flight.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AvailableFlightLegService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight Number
		int flight = StringUtils.parse(ctx.getParameter("flight"), 1);
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		if (a == null)
			a = SystemData.getAirline(SystemData.get("airline.code"));
		
		int leg = 0;
		try {
			GetRawScheduleInfo dao = new GetRawScheduleInfo(ctx.getConnection());
			leg = dao.getNextLeg(a, flight);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("airline", a.getCode());
		jo.put("number", flight);
		jo.put("leg", leg);

		// Dump the XML to the output stream
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
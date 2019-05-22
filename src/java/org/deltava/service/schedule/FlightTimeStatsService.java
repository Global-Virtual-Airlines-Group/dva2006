// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Airport arrival/departure time distributions.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class FlightTimeStatsService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the Airport
		Airport a = SystemData.getAirport(ctx.getParameter("airport"));
		if (a == null)
			return SC_NOT_FOUND;
		
		List<ScheduleStatsEntry> stats = IntStream.range(0, 24).mapToObj(hr -> new ScheduleStatsEntry(hr)).collect(Collectors.toList());
		try {
			GetScheduleAirport sadao = new GetScheduleAirport(ctx.getConnection());
			sadao.getDepartureStatistics(a).stream().forEach(se -> { stats.get(se.getHour()).setDepartureLegs(se.getDomesticDepartureLegs(), se.getInternationalDepartureLegs()); });
			sadao.getArrivalStatistics(a).stream().forEach(se -> { stats.get(se.getHour()).setArrivalLegs(se.getDomesticArrivalLegs(), se.getInternationalArrivalLegs()); });
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} finally {
			ctx.release();
		}
		
		// Create the JSONObject
		JSONObject jo = new JSONObject();
		jo.put("airport", a.getICAO());
		for (ScheduleStatsEntry se : stats) {
			JSONObject ho = new JSONObject();
			ho.put("hour", se.getHour());
			ho.put("dd", se.getDomesticDepartureLegs());
			ho.put("di", se.getInternationalDepartureLegs());
			ho.put("ad", se.getDomesticArrivalLegs());
			ho.put("ai", se.getInternationalArrivalLegs());
			jo.append("flights", ho);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "flights");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(1800);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}
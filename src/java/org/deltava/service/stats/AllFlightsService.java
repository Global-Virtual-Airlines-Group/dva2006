// Copyright 2007, 2008, 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to display Flight Report statistics to an Google chart.
 * @author Luke
 * @version 6.4
 * @since 2.1
 */

public class AllFlightsService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we're displaying legs or hours
		boolean isHours = Boolean.valueOf(ctx.getParameter("hours")).booleanValue();
		
		// Get the Flight Report statistics - remove the last entry
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		try {
			GetAggregateStatistics stdao = new GetAggregateStatistics(ctx.getConnection());
			results.addAll(stdao.getPIREPStatistics(FlightStatsSort.DATE, FlightStatsGroup.MONTH));
			if (!results.isEmpty())
				results.remove(0);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		Collections.reverse(results);
		JSONArray ja = new JSONArray();
		for (FlightStatsEntry entry : results) {
			JSONArray ea = new JSONArray();
			ea.put(entry.getLabel());
			ea.put(isHours ? (int)entry.getHours() : entry.getLegs());
			ea.put(entry.getOnlineLegs());
			ea.put(entry.getACARSLegs());
			ea.put(entry.getHistoricLegs());
			ja.put(ea);
		}
		
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (Exception e) {
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
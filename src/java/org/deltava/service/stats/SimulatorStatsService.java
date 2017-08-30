// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to graph simulator version statistics.
 * @author Luke
 * @version 7.5
 * @since 7.4
 */

public class SimulatorStatsService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<FlightStatsEntry> stats = new ArrayList<FlightStatsEntry>();
		try {
			GetAggregateStatistics dao = new GetAggregateStatistics(ctx.getConnection());
			stats.addAll(dao.getSimStatistics(FlightStatsSort.DATE, FlightStatsGroup.MONTH));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the JSON object
		JSONArray ja = new JSONArray(); final Integer ZERO = Integer.valueOf(0);
		for (FlightStatsEntry e : stats) {
			Map<String, Integer> legs = e.getVersionLegs();
			JSONArray ma = new JSONArray();
			ma.put(e.getLabel());
			ma.put(legs.getOrDefault("P3D", ZERO).intValue() + legs.getOrDefault("P3Dv4", ZERO).intValue());
			ma.put(legs.getOrDefault("FSX", ZERO).intValue());
			ma.put(legs.getOrDefault("FS9", ZERO).intValue());
			ma.put(legs.getOrDefault("XP10", ZERO).intValue() + legs.getOrDefault("XP11", ZERO).intValue());
			ma.put(legs.getOrDefault("FS2002", ZERO).intValue());
			ma.put(legs.getOrDefault("UNKNOWN", ZERO).intValue() + legs.getOrDefault("FS2000", ZERO).intValue());
			ja.put(ma);
		}

		// Dump the JSON to the output stream
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
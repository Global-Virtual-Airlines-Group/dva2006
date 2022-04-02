// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to display online flight statistics. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class OnlineStatsService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<OnlineStatsEntry> stats = new ArrayList<OnlineStatsEntry>();
		try {
			GetAggregateStatistics stdao = new GetAggregateStatistics(ctx.getConnection());
			stats.addAll(stdao.getOnlineStatistics(FlightStatsSort.DATE, FlightStatsGroup.MONTH));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the JSON
		JSONArray ja = new JSONArray();
		for (OnlineStatsEntry e : stats) {
			JSONArray ma = new JSONArray();
			ma.put(e.getLabel());
			ma.put(e.getTotalLegs());
			ma.put(e.getLegs(OnlineNetwork.VATSIM));
			ma.put(e.getLegs(OnlineNetwork.IVAO));
			ma.put(e.getLegs(OnlineNetwork.PILOTEDGE));
			ma.put(e.getLegs(OnlineNetwork.POSCON));
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

	@Override
	public final boolean isLogged() {
		return false;
	}
}
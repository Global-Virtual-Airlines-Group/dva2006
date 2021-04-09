// Copyright 2018, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.stats.ClientBuildStats;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS client build statistics.
 * @author Luke
 * @version 10.0
 * @since 8.2
 */

public class ClientVersionService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get count
		int weeks = StringUtils.parse(ctx.getParameter("count"), 12);
		final Collection<ClientBuildStats> stats = new ArrayList<ClientBuildStats>();
		try {
			GetACARSBuilds dao = new GetACARSBuilds(ctx.getConnection());
			stats.addAll(dao.getBuildStatistics(weeks));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Build the list of builds
		Collection<Integer> builds = new TreeSet<Integer>();
		stats.stream().map(ClientBuildStats::getBuilds).flatMap(Collection::stream).forEach(builds::add);
		
		// Build the JSON object
		JSONObject jo = new JSONObject();
		jo.put("builds", new JSONArray(builds));
		jo.put("weeks", weeks);
		JSONArray ja = new JSONArray();
		jo.put("stats", ja);
		for (ClientBuildStats entry : stats) {
			JSONObject eo = new JSONObject();
			eo.put("week", JSONUtils.formatDate(entry.getDate()));
			for (Integer b : builds) {
				JSONObject bo = new JSONObject();
				Tuple<Integer, Double> data = entry.getCount(b.intValue());
				bo.put("legs", (data == null) ? 0 : data.getLeft().intValue());
				bo.put("hours", (data == null) ? 0 : data.getRight().doubleValue());
				eo.put(b.toString(), bo);
			}
			
			ja.put(eo);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "stats");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}
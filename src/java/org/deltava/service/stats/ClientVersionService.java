// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

import org.deltava.beans.stats.ClientBuildStats;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS client build statistics.
 * @author Luke
 * @version 8.2
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
		int months = StringUtils.parse(ctx.getParameter("count"), 12);
		final Collection<ClientBuildStats> stats = new ArrayList<ClientBuildStats>();
		try {
			GetACARSBuilds dao = new GetACARSBuilds(ctx.getConnection());
			dao.setQueryMax(months);
			stats.addAll(dao.getBuildStatistics());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Build the list of builds
		
		// Build the JSON object
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		jo.put("months", months);
		jo.put("stas", ja);
		for (ClientBuildStats entry : stats) {
			JSONArray ea = new JSONArray();
			ea.put(JSONUtils.format(entry.getDate()));
			
			ja.put(ea);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "stats");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(60);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}
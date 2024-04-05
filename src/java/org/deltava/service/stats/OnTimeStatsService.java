// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.flight.OnTime;
import org.deltava.beans.stats.OnTimeStatsEntry;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display airline-wide on time statistics.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class OnTimeStatsService extends WebService {
	
	private final List<OnTime> OT = List.of(OnTime.EARLY, OnTime.ONTIME, OnTime.LATE);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		int daysBack = Math.min(720, StringUtils.parse(ctx.getParameter("id"), 365));
		List<OnTimeStatsEntry> results = new ArrayList<OnTimeStatsEntry>();
		try {
			GetACARSOnTime dao = new GetACARSOnTime(ctx.getConnection());
			results.addAll(dao.getByDate(daysBack));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Build the JSON document
		JSONObject jo = new JSONObject(); int maxLegs = 0;
		jo.put("days", daysBack);
		for (OnTimeStatsEntry st : results) {
			JSONArray ja = new JSONArray(); int totalLegs = 0;
			ja.put(JSONUtils.formatDate(st.getDate()));
			for (OnTime ot : OT) {
				int legs = st.getLegs(ot);
				totalLegs += legs;
				ja.put(legs);
			}

			maxLegs = Math.max(maxLegs, totalLegs);
			jo.accumulate("data", ja);
		}
		
		// Dump to the output stream
		jo.put("maxLegs", maxLegs + (maxLegs % 20));
		JSONUtils.ensureArrayPresent(jo, "data");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
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
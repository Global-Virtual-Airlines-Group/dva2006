// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.*;
import java.time.format.*;

import org.json.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display flight load factor statistics.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class LoadStatsService extends WebService {
	
	private final DateTimeFormatter DF = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the load generator
		EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
		if (eInfo == null)
			throw error(SC_INTERNAL_SERVER_ERROR, "No Economy data for Airline");
		
		int daysBack = Math.min(720, StringUtils.parse(ctx.getParameter("id"), 365));
		Map<String,LoadStatistics> results = new HashMap<String,LoadStatistics>();
		try {
			GetFlightReportLoad lsdao = new GetFlightReportLoad(ctx.getConnection());
			Collection<LoadStatistics> data = lsdao.getLoad(daysBack, FlightStatsGroup.DATE);
			results.putAll(CollectionUtils.createMap(data, LoadStatistics::getLabel));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Build the JSON object
		int maxPax = results.values().stream().mapToInt(LoadStatistics::getPax).max().orElse(1);
		JSONObject jo = new JSONObject();
		jo.put("days", daysBack);
		jo.put("maxPassengers", maxPax + (maxPax % 1000));
		
		// Build the dates and load factors
		LoadFactor lf = new LoadFactor(eInfo);
		LocalDate ld = LocalDate.now();
		for (int db = 1; db < daysBack; db++) {
			LocalDateTime ldt = ld.minusDays(db).atTime(12, 0); Instant d = ldt.toInstant(ZoneOffset.UTC);
			String dt = DF.format(ldt);
			LoadStatistics ls = results.getOrDefault(dt, new LoadStatistics(dt));
			JSONArray da = new JSONArray();
			da.put(JSONUtils.formatDate(d));
			da.put(ls.getLoadFactor());
			da.put(lf.getTargetLoad(d));
			da.put(ls.getPax());
			jo.accumulate("data", da);
		}
		
		// Dump to the output stream
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
	public final boolean isSecure() {
		return true;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}
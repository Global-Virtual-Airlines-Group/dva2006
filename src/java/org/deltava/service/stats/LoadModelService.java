// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.*;

import org.json.*;

import org.deltava.beans.econ.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to model load factor adjustments.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class LoadModelService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the default values
		EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
		if (eInfo == null)
			throw error(SC_INTERNAL_SERVER_ERROR, "No Economy data for Airline");
		
		// Load modeled values
		EconomyInfo e2 = new EconomyInfo(StringUtils.parse(ctx.getParameter("targetLoad"), eInfo.getTargetLoad()), StringUtils.parse(ctx.getParameter("targetAmp"), eInfo.getAmplitude()));
		e2.setMinimumLoad(StringUtils.parse(ctx.getParameter("minLoad"), 0.2));
		e2.setStartDate(eInfo.getStartDate());
		e2.setCycleLength(StringUtils.parse(ctx.getParameter("cycleLength"), 365));
		int daysBack = StringUtils.parse(ctx.getParameter("daysBack"), 90);
		
		// Create load geenerators
		LoadFactor lf = new LoadFactor(eInfo);
		LoadFactor lf2 = new LoadFactor(e2);
		
		// Build the JSON document
		JSONObject jo = new JSONObject();
		jo.put("days", daysBack);
		jo.put("model", serialize(e2));
		jo.put("defaultModel", serialize(eInfo));

		// Calculate the modeled load factor
		LocalDate ld = LocalDate.now();
		for (int day = 1; day < daysBack; day++) {
			ld = ld.minusDays(1);
			JSONArray ja = new JSONArray();
			Instant dt = ld.atTime(12, 0).toInstant(ZoneOffset.UTC);
			ja.put(JSONUtils.formatDate(dt));
			ja.put(lf.getTargetLoad(dt));
			ja.put(lf2.getTargetLoad(dt));
			jo.accumulate("data", ja);
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
	
	/*
	 * Helper method to serialize model data.
	 */
	private static JSONObject serialize(EconomyInfo ei) {
		JSONObject mo = new JSONObject();
		mo.put("targetLoad", ei.getTargetLoad());
		mo.put("targetAmp", ei.getAmplitude());
		mo.put("yearHZ", ei.getCyclesPerYear());
		mo.put("cycleLength", ei.getCycleLength());
		return mo;
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
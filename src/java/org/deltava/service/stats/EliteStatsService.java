// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.EliteStats;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.JSONUtils;

/**
 * A Web Service to display Elite program statistics.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteStatsService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		int currentYear = EliteLevel.getYear(Instant.now());
		List<EliteLevel> lvls = new ArrayList<EliteLevel>();
		Map<String, Collection<Integer>> stats = new LinkedHashMap<String, Collection<Integer>>();
		List<EliteStats> yrStats = new ArrayList<EliteStats>(); Collection<Integer> yrs = new LinkedHashSet<Integer>(); 
		try {
			Connection con = ctx.getConnection();
			
			// Load levels
			GetElite eldao = new GetElite(con);
			lvls.addAll(eldao.getLevels());
			
			// Load yearly counts
			for (int yr = EliteLevel.MIN_YEAR; yr <= currentYear; yr++) {
				yrs.add(Integer.valueOf(yr));
				Map<EliteLevel, Integer> cnts = eldao.getEliteCounts(yr);
				for (Map.Entry<EliteLevel, Integer> me : cnts.entrySet()) {
					Collection<Integer> cnt = stats.get(me.getKey().getName());
					if (cnt == null) {
						cnt = new ArrayList<Integer>();
						stats.put(me.getKey().getName(), cnt);
					}
					
					cnt.add(me.getValue());
				}
			}
			
			// Load current stats and deviations
			GetEliteStatistics elsdao = new GetEliteStatistics(con);
			yrStats.addAll(elsdao.getStatistics(currentYear));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the JSON document and yearly counts
		JSONObject jo = new JSONObject();
		jo.put("years", new JSONArray(yrs));
		jo.put("currentYear", currentYear);
		for (Map.Entry<String, Collection<Integer>> me : stats.entrySet()) {
			JSONObject so = new JSONObject();
			EliteLevel lvl = lvls.stream().filter(lv -> lv.getName().equals(me.getKey())).findAny().orElse(null);
			so.put("name", lvl.getName());
			so.put("color", lvl.getHexColor());
			so.put("data", new JSONArray(me.getValue()));
			jo.append("levels", so);
		}
		
		// Create the yearly requirements
		for (String lvlName : stats.keySet()) {
			JSONObject so = new JSONObject();
			Collection<EliteLevel> yrLevels = lvls.stream().filter(lv -> lv.getName().equals(lvlName)).collect(Collectors.toList());
			so.put("name", lvlName);
			so.put("legs", yrLevels.stream().map(EliteLevel::getLegs).collect(Collectors.toList()));
			so.put("distance", yrLevels.stream().map(EliteLevel::getDistance).collect(Collectors.toList()));
			jo.append("reqs", so);
		}
		
		// Add the level averages
		for (EliteStats st : yrStats) {
			JSONObject so = new JSONObject();
			JSONArray sea = new JSONArray();
			EliteLevel lvl = st.getLevel();
			so.put("name", lvl.getName());
			so.put("color", lvl.getHexColor());
			sea.put(st.getLevel().getName());
			sea.put(st.getPilots());
			sea.put(st.getLegs());
			sea.put(st.getDistance());
			sea.put(st.getPilots());
			sea.put(st.getMaxLegs());
			sea.put(st.getMaxDistance());
			sea.put(st.getLegSD());
			sea.put(st.getDistanceSD());
			so.put("data", sea);
			jo.append("stats", so);
		}
		
		// Dump to the output stream
		JSONUtils.ensureArrayPresent(jo, "stats", "levels", "reqs");
		try {
			ctx.setContentType("application/json", "UTF-8");
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
}
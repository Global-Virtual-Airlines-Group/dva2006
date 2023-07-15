// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 11.0
 * @since 9.2
 */

public class EliteStatsService extends WebService {
	
	private class EliteLevelComparator implements Comparator<EliteLevel> {

		@Override
		public int compare(EliteLevel el1, EliteLevel el2) {
			int tmpResult = Integer.compare(el1.getYear(), el2.getYear());
			return (tmpResult == 0) ? el1.compareTo(el2) : tmpResult;
		}
	}

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		final int currentYear = EliteLevel.getYear(Instant.now());
		SortedSet<EliteLevel> allLevels = new TreeSet<EliteLevel>(new EliteLevelComparator()); SortedSet<EliteLevel> levelLegend = new TreeSet<EliteLevel>();
		Map<EliteLevel, Integer> allCounts = new TreeMap<EliteLevel, Integer>(new EliteLevelComparator());
		List<EliteStats> yrStats = new ArrayList<EliteStats>(); Collection<Integer> yrs = new LinkedHashSet<Integer>(); 
		try {
			Connection con = ctx.getConnection();
			
			// Load current levels - assume they have been constant, but we will add if we need to
			GetElite eldao = new GetElite(con);
			allLevels.addAll(eldao.getLevels());
			allLevels.stream().filter(lv -> (lv.getYear() == currentYear)).forEach(levelLegend::add);
			
			// Load all of the pilot/level/year counts
			for (int yr = EliteLevel.MIN_YEAR; yr <= currentYear; yr++) {
				yrs.add(Integer.valueOf(yr));
				allCounts.putAll(eldao.getEliteCounts(yr));
			}

			// Load current stats and deviations
			GetEliteStatistics elsdao = new GetEliteStatistics(con);
			yrStats.addAll(elsdao.getStatistics(currentYear));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Aggregate into stats set, by year
		Map<String, List<Integer>> stats = new LinkedHashMap<String, List<Integer>>();
		for (Map.Entry<EliteLevel, Integer> me : allCounts.entrySet()) {
			EliteLevel el = me.getKey();
			if (!levelLegend.stream().anyMatch(el::matches))
				levelLegend.add(me.getKey());
			
			// Get the count, and set to number of years
			List<Integer> cnts = stats.getOrDefault(el.getName(), new ArrayList<Integer>());
			while (cnts.size() < yrs.size())
				cnts.add(Integer.valueOf(0));
			
			// Populate and set
			cnts.set(el.getYear() - EliteLevel.MIN_YEAR, me.getValue());
			stats.putIfAbsent(el.getName(), cnts);
		}

		// Create the JSON document and level defintiions/requirements
		JSONObject jo = new JSONObject();
		jo.put("years", new JSONArray(yrs));
		jo.put("currentYear", currentYear);
		
		// Write the legend
		for (EliteLevel el : levelLegend) {
			JSONObject so = new JSONObject();
			so.put("name", el.getName());
			so.put("color", el.getHexColor());
			jo.append("levels", so);
		}
		
		// Create the yearly requirements
		JSONObject jro = new JSONObject();
		jo.put("reqs", jro);
		for (EliteLevel lvl : levelLegend) {
			JSONObject so = new JSONObject(); Collection<EliteLevel> yrLevels = new TreeSet<EliteLevel>(new EliteLevelComparator());
			allLevels.stream().filter(lv -> lvl.matches(lv)).forEach(yrLevels::add);
			so.put("legs", yrLevels.stream().map(EliteLevel::getLegs).collect(Collectors.toList()));
			so.put("distance", yrLevels.stream().map(EliteLevel::getDistance).collect(Collectors.toList()));
			jro.put(lvl.getName(), so);
		}
		
		// Write yearly counts
		JSONObject lco = new JSONObject();
		stats.entrySet().forEach(me -> lco.put(me.getKey(), new JSONArray(me.getValue())));
		jo.put("levelCounts", lco);
		
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
			//jo.append("stats", so);
		}
		
		// Dump to the output stream
		JSONUtils.ensureArrayPresent(jo, "stats", "levels");
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
}
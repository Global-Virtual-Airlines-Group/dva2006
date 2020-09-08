// Copyright 2007, 2008, 2009, 2010, 2012, 2015, 2016, 2017, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import java.util.*;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display a Pilot's Flight Report statistics to a Google chart.
 * @author Luke
 * @version 9.1
 * @since 2.1
 */

public class MyFlightsService extends WebService {
	
	private static final int MAX_ENTRIES = 12;
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;

		// Get the Flight Report statistics
		Collection<FlightStatsEntry> results = null; 
		Collection<StageStatsEntry> stageStats = null;
		Collection<SimStatsEntry> simStats = null;
		Collection<LandingStatistics> landings = null;
		Map<Integer, Integer> vsStats = null;
		try {
			// Load statistics
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(ctx.getConnection());
			results = stdao.getPIREPStatistics(userID, FlightStatsSort.LEGS, FlightStatsGroup.EQ);
			stageStats = stdao.getStageStatistics(userID);
			simStats = stdao.getSimulatorStatistics(userID);
			vsStats = stdao.getLandingCounts(userID, 50);
			landings = stdao.getLandingData(userID);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the equipment stats
		JSONObject jo = new JSONObject();
		JSONArray ee = new JSONArray();
		ee.put("All Others");

		// Create the entries
		int entryCount = 0; int eeValue = 0;
		for (FlightStatsEntry entry : results) {
			entryCount++;
			
			// Add value
			if (entryCount <= MAX_ENTRIES) {
				JSONArray ea = new JSONArray();
				ea.put(entry.getLabel());
				ea.put(entry.getLegs());
				jo.append("eqCount", ea);
			}
			else
				eeValue += entry.getLegs();
		}

		// Add the "everything else" entry
		ee.put(eeValue);
		jo.append("eqCount", ee);
		
		// Create landing rate groups - this will be a stacked bar chart
		for (Map.Entry<Integer, Integer> me : vsStats.entrySet()) {
			int fpm = me.getKey().intValue();
			JSONArray ea = new JSONArray();  
			ea.put(me.getKey().toString() + " ft/min");
			ea.put((fpm < -600) ? me.getValue().intValue() : 0);
			ea.put((fpm < -300) && (fpm >= -600) ? me.getValue().intValue() : 0);
			ea.put((fpm < -50) && (fpm >= -300) ? me.getValue().intValue() : 0);
			ea.put((fpm <= 0) && (fpm >= -50) ? me.getValue().intValue() : 0);
			jo.append("landingSpd", ea);
		}
		
		// Go through landing statistics
		int[] qualCount = new int[] {0, 0, 0};
		for (LandingStatistics ls : landings) {
			JSONArray ea = new JSONArray();
			FlightScore score = FlightScorer.score(ls);
			if (score == FlightScore.INCOMPLETE) continue;
			qualCount[score.ordinal()]++;
			
			int fpm = (int) ls.getAverageSpeed();
			boolean tooSoft = (fpm > -74);
			
			// Save touchdown scatter chart
			ea.put((int) ls.getAverageDistance());
			ea.put((score == FlightScore.DANGEROUS) ? Integer.valueOf(fpm) : null);
			ea.put(!tooSoft && (score == FlightScore.ACCEPTABLE) ? Integer.valueOf(fpm) : null);
			ea.put((score == FlightScore.OPTIMAL) ? Integer.valueOf(fpm) : null);
			ea.put(tooSoft ? Integer.valueOf(fpm) : null);
			jo.append("landingSct", ea);
		}
		
		// Convert qualitative info into an array
		for (int x = 0; x < qualCount.length; x++) {
			JSONArray ea = new JSONArray();
			ea.put(FlightScore.values()[x].getName());
			ea.put(qualCount[x]);
			jo.append("landingQuality", ea);
		}
		
		// Create stage/flights by Month
		int maxStage = stageStats.stream().mapToInt(StageStatsEntry::getMaxStage).max().orElse(1);
		jo.put("maxStage", maxStage);
		JSONArray jdo = new JSONArray(); JSONArray jdh = new JSONArray();
		for (StageStatsEntry entry : stageStats) {
			JSONObject jd = JSONUtils.format(entry.getDate());
			JSONArray da = new JSONArray(); JSONArray dh = new JSONArray();
			da.put(jd); dh.put(jd);
			
			for (int x = 1; x <= maxStage; x++) {
				da.put(entry.getLegs(x));
				dh.put(entry.getHours(x));
			}

			jdo.put(da); jdh.put(dh);
		}
		
		// Create sim/flights by Month
		Collection<Simulator> sims = simStats.stream().flatMap(ss -> ss.getKeys().stream()).collect(Collectors.toCollection(TreeSet::new));
		sims.forEach(s -> jo.append("sims", s.name()));
		JSONArray jso = new JSONArray(); JSONArray jsh = new JSONArray();
		for (SimStatsEntry entry : simStats) {
			JSONObject jd = JSONUtils.format(entry.getDate());
			JSONArray da = new JSONArray(); JSONArray dh = new JSONArray();
			da.put(jd); dh.put(jd);
			sims.forEach(s -> { da.put(entry.getLegs(s)); dh.put(entry.getHours(s)); });
			jso.put(da); jsh.put(dh);
		}
		
		// Dump the JSON to the output stream
		jo.put("calendar", jdo); jo.put("calendarHours", jdh);
		jo.put("simCalendar", jso); jo.put("simCalendarHours", jsh);
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

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}
// Copyright 2007, 2008, 2009, 2010, 2012, 2015, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display statistics about a Pilot's landings.
 * @author Luke
 * @version 11.0
 * @since 2.1
 */

public class MyFlightStatsCommand extends AbstractViewCommand {
	
	private static final List<ComboAlias> GRAPH_OPTS = ComboUtils.fromArray(new String[] {"Flight Legs", "Flight Hours", "Flight Distance"}, new String[] {"LEGS", "HOURS", "DISTANCE"});
	
	private final class LandingStatsComparator implements Comparator<LandingStatistics> {
		@Override
		public int compare(LandingStatistics ls1, LandingStatistics ls2) {
			int tmpResult = Double.compare(ls1.getAverageScore(), ls2.getAverageScore());
			return (tmpResult == 0) ? ls1.getEquipmentType().compareTo(ls2.getEquipmentType()) : tmpResult;
		}
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get grouping / sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class);
		FlightStatsSort srt = EnumUtils.parse(FlightStatsSort.class, vc.getSortType(), FlightStatsSort.LEGS);
		FlightStatsGroup grp = EnumUtils.parse(FlightStatsGroup.class, ctx.getParameter("groupType"), FlightStatsGroup.EQ);
		vc.setSortType(srt.name()); ctx.setAttribute("groupType", grp, REQUEST);
		ctx.setAttribute("graphOpts", GRAPH_OPTS, REQUEST);

		// Get the user ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		// Get the number of days to retrieve
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			// Load legs
			GetFlightReportACARS frdao = new GetFlightReportACARS(con);
			if (p.getACARSLegs() < 0)
				frdao.getOnlineTotals(p, ctx.getDB());
			
			// Get the Flight Report statistics
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			vc.setResults(stdao.getPIREPStatistics(userID, srt, grp));
			List<LandingStatistics> landingStats = stdao.getLandings(userID);
			landingStats.sort(new LandingStatsComparator().reversed());
			ctx.setAttribute("eqLandingStats", landingStats, REQUEST);
			ctx.setAttribute("eqLandingSortData", landingStats.stream().map(MyFlightStatsCommand::toJSON).collect(Collectors.toList()), REQUEST);
			
			// Get popular route pairs
			stdao.setQueryMax(30);
			Collection<RouteStats> popRoutes = stdao.getPopularRoutes(userID);
			ctx.setAttribute("popularRoutes", popRoutes, REQUEST);
			ctx.setAttribute("popRouteSortData", popRoutes.stream().map(MyFlightStatsCommand::toJSON).collect(Collectors.toList()), REQUEST);
			ctx.setAttribute("popularTotal", Integer.valueOf(popRoutes.stream().mapToInt(RouteStats::getFlights).sum()), REQUEST);
			
			// Get my best landings
			GetFlightReportRecognition frrdao = new GetFlightReportRecognition(con); 
			frrdao.setQueryMax(25); frrdao.setDayFilter(365);
			Collection<Integer> landingIDs = frrdao.getGreasedLandings(userID);
			
			// Load PIREPs and runway data
			GetACARSData acdao = new GetACARSData(con);
			Collection<FlightReport> pireps = new ArrayList<FlightReport>(); Collection<JSONObject> jla = new ArrayList<JSONObject>();
			Map<Integer, RunwayDistance> runways = new HashMap<Integer, RunwayDistance>();
			for (Integer pirepID : landingIDs) {
				FlightReport fr = frdao.get(pirepID.intValue(), ctx.getDB());
				pireps.add(fr);
				RunwayDistance rd = acdao.getLandingRunway(fr.getDatabaseID(DatabaseID.ACARS));
				if ((rd != null) && (fr instanceof FDRFlightReport ffr)) {
					runways.put(pirepID, rd);
					jla.add(toJSON(ffr, rd));
				}
			}
			
			ctx.setAttribute("bestLandings", pireps, REQUEST);
			ctx.setAttribute("rwyDistance", runways, REQUEST);
			ctx.setAttribute("bestLandingSince", pireps.stream().map(FlightReport::getDate).min(Comparator.naturalOrder()).orElse(null), REQUEST);
			
			// Add sort data
			ctx.setAttribute("landingSortData", jla, REQUEST);
			ctx.setAttribute("statSortData", vc.getResults().stream().map(JSONUtils::format).collect(Collectors.toList()), REQUEST);

			// Get pilot and totals
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("totalLegs", Integer.valueOf(vc.getResults().stream().mapToInt(FlightStatsEntry::getLegs).sum()),REQUEST);
			ctx.setAttribute("acarsLegs", Integer.valueOf(landingStats.stream().mapToInt(LandingStatistics::getLegs).sum()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/myStats.jsp");
		result.setSuccess(true);
	}
	
	/*
	 * Helper method to convert landing data to JSON.
	 */
	private static JSONObject toJSON(FDRFlightReport fr, RunwayDistance rd) {
		JSONObject jo = new JSONObject();
		jo.put("id", fr.getID());
		jo.put("date", fr.getDate().toEpochMilli());
		jo.put("flight", fr.getFlightCode());
		jo.put("eqType", fr.getEquipmentType());
		jo.put("rwyDistance", rd.getDistance());
		jo.put("vSpeed", fr.getLandingVSpeed());
		jo.put("score", fr.getLandingScore());
		return jo;
	}
	
	/*
	 * Helper method to convert route statistics to JSON.
	 */
	private static JSONObject toJSON(RouteStats st) {
		JSONObject jo = new JSONObject();
		jo.put("id", st.createKey());
		jo.put("airportD", st.getAirportD().getIATA());
		jo.put("airportA", st.getAirportA().getIATA());
		jo.put("legs", st.getFlights());
		jo.put("acars", st.getACARSFlights());
		jo.put("distance", st.getDistance());
		jo.put("lastFlight", st.getLastFlight().toEpochMilli());
		return jo;
	}
	
	public static JSONObject toJSON(LandingStatistics ls) {
		JSONObject jo = new JSONObject();
		jo.put("id", ls.getEquipmentType());
		jo.put("legs", ls.getLegs());
		jo.put("hours", ls.getHours());
		jo.put("vSpeed", ls.getAverageSpeed());
		jo.put("vSpeedSD", ls.getStdDeviation());
		jo.put("distance", ls.getAverageDistance());
		jo.put("distanceSD", ls.getDistanceStdDeviation());
		jo.put("score", ls.getAverageScore());
		return jo;
	}
}
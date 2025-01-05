// Copyright 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display a Pilot's Elite status history. 
 * @author Luke
 * @version 11.4
 * @since 9.2
 */

public class EliteInfoCommand extends AbstractCommand {
	
	private static class StatusComparator implements Comparator<EliteStatus> {
		@Override
		public int compare(EliteStatus es1, EliteStatus es2) {
			return es1.compareTo(es2);
		}
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the user ID
		int id = ctx.getUser().getID();
		if ((ctx.isUserInRole("Operations") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			id = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);
			
			// Get all elite levels
			GetElite eldao = new GetElite(con);
			Collection<EliteLevel> lvls = eldao.getLevels().stream().filter(lvl -> lvl.getLegs() > 0).collect(Collectors.toList());
			Map<Integer, Collection<EliteLevel>> yearlyLevels = new HashMap<Integer, Collection<EliteLevel>>();
			lvls.forEach(lvl -> CollectionUtils.addMapCollection(yearlyLevels, Integer.valueOf(lvl.getYear()), lvl));
			
			// Determine the year and whether things are populated
			final Instant now = Instant.now(); int statsYear = EliteScorer.getStatsYear(now); int statusYear = EliteScorer.getStatusYear(now);
			boolean isRollover = statsYear > statusYear;
			boolean hasCurrentLevels = yearlyLevels.containsKey(Integer.valueOf(statsYear));
			EliteStatus currentStatus = eldao.getStatus(p.getID(), statsYear);
			
			// Check if we're ready to start the new year (levels populated and rollover complete)
			int currentYear = statusYear;
			if (isRollover && hasCurrentLevels && (currentStatus != null)) {
				currentYear = statsYear;
				isRollover = false;
			} else if (currentStatus == null)
				currentStatus = eldao.getStatus(p.getID(), statusYear);
			
			// Get this year's levels
			SortedSet<EliteLevel> cyLevels = new TreeSet<EliteLevel>(yearlyLevels.get(Integer.valueOf(currentYear)));
			if (currentStatus == null)
				currentStatus = new EliteStatus(p.getID(), cyLevels.first());
			
			// Get status history
			Map<Integer, EliteStatus> yearMax = new TreeMap<Integer, EliteStatus>();
			Map<Integer, Collection<EliteStatus>> yearlyStatusUpdates = new TreeMap<Integer, Collection<EliteStatus>>();
			eldao.getAllStatus(p.getID(), 0).forEach(est -> CollectionUtils.addMapCollection(yearlyStatusUpdates, Integer.valueOf(est.getLevel().getYear()), est));
			for (Map.Entry<Integer, Collection<EliteStatus>> me : yearlyStatusUpdates.entrySet()) {
				Optional<EliteStatus> maxStatus = me.getValue().stream().max(new StatusComparator());
				if (!maxStatus.isEmpty())
					yearMax.put(me.getKey(), maxStatus.get());
			}
			
			// Get the Pilot's history
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			Map<Integer, YearlyTotal> totals = CollectionUtils.createMap(esdao.getEliteTotals(p.getID()), YearlyTotal::getYear);
			totals.putIfAbsent(Integer.valueOf(currentYear), new YearlyTotal(currentYear, p.getID()));
			ctx.setAttribute("totals", totals, REQUEST);
			ctx.setAttribute("ro", esdao.getRollover(p.getID(), currentYear), REQUEST);
			if (isRollover)
				ctx.setAttribute("ny", totals.get(Integer.valueOf(currentYear + 1)), REQUEST);
			
			// Load unscored flight IDs
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter(30);
			Collection<Integer> unscoredFlightIDs = frsdao.getUnscoredFlights();
			
			// Get and score pending flights
			EliteScorer es = EliteScorer.getInstance(); YearlyTotal pndt = new YearlyTotal(currentYear, id);
			GetFlightReports frdao = new GetFlightReports(con);
			EliteLevel lvl = currentStatus.getLevel(); final int cy = currentYear;
			List<FlightReport> pendingFlights = frdao.getLogbookCalendar(p.getID(), ctx.getDB(), Instant.now().minusSeconds(Duration.ofDays(30).toSeconds()), 30);
			pendingFlights.removeIf(fr -> !isPending(fr, cy, unscoredFlightIDs));
			pendingFlights.stream().map(fr -> { fr.setStatus(FlightStatus.OK); return es.score(fr, lvl); }).forEach(pndt::add);
			ctx.setAttribute("pending", pndt, REQUEST);
			
			// Calculate next year's status
			YearlyTotal yt = totals.get(Integer.valueOf(currentYear));
			EliteLevel nyLevel = yt.matches(cyLevels);
			ctx.setAttribute("nextYearLevel", nyLevel, REQUEST);
			
			// Calculate projections
			int m = LocalDate.now().getMonthValue();
			if (m > 3) {
				YearlyTotal pt = yt.adjust(LocalDate.now());
				ctx.setAttribute("projectedTotal", pt, REQUEST);
				ctx.setAttribute("projectedLevel", pt.matches(cyLevels), REQUEST);
				
				// Check if almost going to miss
				if (m > 9) {
					EliteLevel nl = cyLevels.stream().filter(lv -> (lv.compareTo(lvl) > 0)).findFirst().orElse(null);
					if (nl != null) {
						YearlyTotal pd = pt.delta(nl);	
						float ld = Math.max(0, pd.getLegs() * 1f / yt.getLegs());
						float dd = Math.max(0, pd.getDistance() * 1f / yt.getDistance());
						ctx.setAttribute("legDelta", Float.valueOf(ld), REQUEST);
						ctx.setAttribute("distDelta", Float.valueOf(dd), REQUEST);
						ctx.setAttribute("nextLevel", nl, REQUEST);
					}
				}
			}
			
			// Save status attributes
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("currentYear", Integer.valueOf(currentYear), REQUEST);
			ctx.setAttribute("isRollover", Boolean.valueOf(isRollover), REQUEST);
			ctx.setAttribute("baseLevel", EliteLevel.EMPTY, REQUEST);
			ctx.setAttribute("levels", yearlyLevels, REQUEST);
			ctx.setAttribute("currentStatus", currentStatus, REQUEST);
			ctx.setAttribute("maxStatus", yearMax, REQUEST);
			ctx.setAttribute("statusUpdates", yearlyStatusUpdates, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteInfo.jsp");
		result.setSuccess(true);
	}
	
	/*
	 * Helper method to determine inclusion in pending flights.
	 */
	private static boolean isPending(FlightReport fr, int yr, Collection<Integer> unscoredIDs) {
		if ((fr.getStatus() == FlightStatus.DRAFT) || (fr.getStatus() == FlightStatus.REJECTED) || (EliteScorer.getStatsYear(fr.getDate()) != yr)) return false;
		return (fr.getStatus() != FlightStatus.OK) || unscoredIDs.contains(Integer.valueOf(fr.getID()));
	}
}
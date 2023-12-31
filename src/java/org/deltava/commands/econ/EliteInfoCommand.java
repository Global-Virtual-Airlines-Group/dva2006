// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 11.1
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
		
		final Integer currentYear = Integer.valueOf(EliteScorer.getStatusYear(Instant.now()));
		boolean isRollover = currentYear.intValue() < EliteScorer.getStatsYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);
			
			// Get this year and next year's levels
			GetElite eldao = new GetElite(con);
			Collection<EliteLevel> lvls = eldao.getLevels().stream().filter(lvl -> lvl.getLegs() > 0).collect(Collectors.toList());
			EliteStatus currentStatus = eldao.getStatus(p.getID(), currentYear.intValue());
			Map<Integer, Collection<EliteLevel>> yearlyLevels = new HashMap<Integer, Collection<EliteLevel>>();
			lvls.forEach(lvl -> CollectionUtils.addMapCollection(yearlyLevels, Integer.valueOf(lvl.getYear()), lvl));
			
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
			totals.putIfAbsent(currentYear, new YearlyTotal(currentYear.intValue(), p.getID()));
			ctx.setAttribute("totals", totals, REQUEST);
			ctx.setAttribute("ro", esdao.getRollover(p.getID(), currentYear.intValue()), REQUEST);
			
			// Load unscored flight IDs
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter(30);
			Collection<Integer> unscoredFlightIDs = frsdao.getUnscoredFlights();
			
			// Get and score pending flights
			EliteScorer es = EliteScorer.getInstance(); YearlyTotal pndt = new YearlyTotal(currentYear.intValue(), id);
			GetFlightReports frdao = new GetFlightReports(con);
			List<FlightReport> pendingFlights = frdao.getLogbookCalendar(p.getID(), ctx.getDB(), Instant.now().minusSeconds(Duration.ofDays(30).toSeconds()), 30);
			pendingFlights.removeIf(fr -> !isPending(fr, currentYear.intValue(), unscoredFlightIDs));
			pendingFlights.stream().map(fr -> { fr.setStatus(FlightStatus.OK); return es.score(fr, currentStatus.getLevel()); }).forEach(pndt::add);
			ctx.setAttribute("pending", pndt, REQUEST);
			
			// Calculate next year's status
			YearlyTotal yt = totals.get(currentYear);
			SortedSet<EliteLevel> cyLevels = new TreeSet<EliteLevel>(yearlyLevels.get(currentYear));
			EliteLevel nyLevel = yt.matches(cyLevels, null);
			ctx.setAttribute("nextYearLevel", nyLevel, REQUEST);
			
			// Calculate projections
			int m = LocalDate.now().getMonthValue();
			if ((m > 3) && (currentStatus != null)) {
				YearlyTotal pt = yt.adjust(LocalDate.now());
				ctx.setAttribute("projectedTotal", pt, REQUEST);
				ctx.setAttribute("projectedLevel", pt.matches(cyLevels, cyLevels.first()), REQUEST);
				
				// Check if almost going to miss
				if (m > 9) {
					EliteLevel nl = cyLevels.stream().filter(lv -> (lv.compareTo(currentStatus.getLevel()) > 0)).findFirst().orElse(null);
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
			ctx.setAttribute("currentYear", currentYear, REQUEST);
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
// Copyright 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;

import org.deltava.comparators.YearlyTotalComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilots at a particular Elite status level. 
 * @author Luke
 * @version 11.5
 * @since 11.0
 */

public class ElitePilotsCommand extends AbstractCommand {
	
	private static final List<ComboAlias> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Flight Legs", SystemData.get("econ.elite.distance")}, new String[] {"0", "1"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int currentYear = EliteScorer.getStatusYear(Instant.now());
		int year = StringUtils.parse(ctx.getParameter("year"), currentYear);
		try {
			Connection con = ctx.getConnection();
			
			// Load the levels
			GetElite eldao = new GetElite(con);
			TreeSet<EliteLevel> lvls = eldao.getLevels(year);
			Map<EliteLevel, EliteLifetime> ltLevels = CollectionUtils.createMap(eldao.getLifetimeLevels(), EliteLifetime::getLevel);
			lvls.removeIf(el -> (el.getLegs() == 0));
			ltLevels.keySet().removeIf(el -> (el.getLegs() == 0));
			
			// Load Pilot totals for current year and rollover
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			List<YearlyTotal> yearTotals = esdao.getPilotTotals(LocalDate.of(year, 1, 1));
			Map<Integer, RolloverYearlyTotal> totalMap = CollectionUtils.createMap(esdao.getRollover(year), YearlyTotal::getID);
			
			// Add yearly totals to rollover
			List<RolloverYearlyTotal> totals = new ArrayList<RolloverYearlyTotal>();
			for (YearlyTotal yt : yearTotals) {
				RolloverYearlyTotal rt = totalMap.getOrDefault(Integer.valueOf(yt.getID()), new RolloverYearlyTotal(year, yt.getID()));
				rt.merge(yt);
				totals.add(rt);
			}
			
			// Sort the merged totals
			Collections.sort(totals, new YearlyTotalComparator(StringUtils.parse(ctx.getParameter("sortType"), YearlyTotalComparator.LEGS)).reversed());
			
			// Load the pilots and totals
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>(); Collection<Integer> lifetimeIDs = new HashSet<Integer>();
			Map<EliteLevel, Collection<YearlyTotal>> lvlTotals = new TreeMap<EliteLevel, Collection<YearlyTotal>>(Comparator.reverseOrder());
			for (EliteLevel lvl : lvls.descendingSet()) {
				Collection<Integer> IDs = new HashSet<Integer>(eldao.getPilots(lvl));
				
				// Load lifetime status
				EliteLifetime lel = ltLevels.get(lvl);
				if (lel != null) {
					Collection<Integer> eltIDs = eldao.getPilots(lel);
					IDs.addAll(eltIDs);
					lifetimeIDs.addAll(eltIDs);
				}
				
				// Get the Pilots - remove inactive if previous year
				IDs.removeAll(pilots.keySet());
				Map<Integer, Pilot> lvPilots = pdao.getByID(IDs, "PILOTS");
				if (year == currentYear) {
					Collection<Integer> inactiveIDs = lvPilots.values().stream().filter(p -> !p.getStatus().isActive()).map(Pilot::getID).collect(Collectors.toSet());
					IDs.removeAll(inactiveIDs); 
					lvPilots.keySet().removeAll(inactiveIDs);
				}
				
				List<YearlyTotal> lt = totals.stream().filter(yt -> IDs.contains(Integer.valueOf(yt.getID()))).collect(Collectors.toList());
				pilots.putAll(lvPilots);
				lvlTotals.put(lvl, lt);
			}
			
			// Get next year's levels
			if (currentYear < year) {
				Collection<EliteLevel> nyLevels = eldao.getLevels(year + 1);
				ctx.setAttribute("nyLevels", CollectionUtils.createMap(nyLevels, EliteLevel::getName), REQUEST);
			} else
				ctx.setAttribute("nyLevels", CollectionUtils.createMap(lvls, EliteLevel::getName), REQUEST);
			
			// Save request attributes
			ctx.setAttribute("pilots", pilots, REQUEST);
			ctx.setAttribute("totals", lvlTotals, REQUEST);
			ctx.setAttribute("year", Integer.valueOf(year), REQUEST);
			ctx.setAttribute("ltIDs", lifetimeIDs, REQUEST);
			ctx.setAttribute("ltLevels", ltLevels, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Build list of years
		Collection<Integer> yrs = new TreeSet<Integer>(Comparator.reverseOrder());
		for (int y = EliteLevel.MIN_YEAR; y <= currentYear; y++)
			yrs.add(Integer.valueOf(y));
		
		ctx.setAttribute("years", yrs, REQUEST);
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);
			
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/elitePilots.jsp");
		result.setSuccess(true);
	}
}
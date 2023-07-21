// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;

import org.deltava.comparators.YearlyTotalComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Pilots at a particular Elite status level. 
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class ElitePilotsCommand extends AbstractCommand {

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
			
			// Load Pilot totals and sort
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			List<YearlyTotal> totals = esdao.getPilotTotals(LocalDate.of(year, 1, 1));
			Collections.sort(totals, new YearlyTotalComparator(YearlyTotalComparator.LEGS).reversed());
			
			// Load the pilots and totals
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			Map<EliteLevel, Collection<YearlyTotal>> lvlTotals = new TreeMap<EliteLevel, Collection<YearlyTotal>>(Comparator.reverseOrder());
			for (EliteLevel lvl : lvls.descendingSet()) {
				Collection<Integer> IDs = eldao.getPilots(lvl); IDs.removeAll(pilots.keySet());
				List<YearlyTotal> lt = totals.stream().filter(yt -> IDs.contains(Integer.valueOf(yt.getID()))).collect(Collectors.toList());
				pilots.putAll(pdao.getByID(IDs, "PILOTS"));
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
			
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/elitePilots.jsp");
		result.setSuccess(true);
	}
}
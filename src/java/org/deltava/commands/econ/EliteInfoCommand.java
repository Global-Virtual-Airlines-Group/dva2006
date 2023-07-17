// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display a Pilot's Elite status history. 
 * @author Luke
 * @version 11.0
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
			
			// Calculate next year's status
			YearlyTotal yt = totals.get(currentYear);
			TreeSet<EliteLevel> cyLevels = new TreeSet<EliteLevel>(yearlyLevels.get(currentYear));
			EliteLevel nyLevel = cyLevels.descendingSet().stream().filter(yt::matches).findFirst().orElse(cyLevels.first());
			ctx.setAttribute("nextYearLevel", nyLevel, REQUEST);
			if (LocalDate.now().getMonthValue() > 3) {
				YearlyTotal pt = yt.adjust(LocalDate.now());
				ctx.setAttribute("projectedTotal", pt, REQUEST);
				ctx.setAttribute("projectedLevel", cyLevels.descendingSet().stream().filter(pt::matches).findFirst().orElse(cyLevels.first()), REQUEST);	
			}
			
			// Save status attributes
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("currentYear", currentYear, REQUEST);
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
}
// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display a Pilot's Elite status history. 
 * @author Luke
 * @version 9.2
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
		
		final int currentYear = EliteLevel.getYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);
			
			// Get this year and next year's levels
			GetElite eldao = new GetElite(con);
			Collection<EliteLevel> lvls = eldao.getLevels();
			EliteStatus currentStatus = eldao.getStatus(p.getID());
			Map<Integer, Collection<EliteLevel>> yearlyLevels = new HashMap<Integer, Collection<EliteLevel>>();
			lvls.forEach(lvl -> CollectionUtils.addMapCollection(yearlyLevels, Integer.valueOf(lvl.getYear()), lvl));
			
			// Get status history
			Map<Integer, EliteStatus> yearMax = new TreeMap<Integer, EliteStatus>();
			Map<Integer, Collection<EliteStatus>> yearlyStatusUpdates = new TreeMap<Integer, Collection<EliteStatus>>();
			eldao.getStatus(p.getID(), 0).forEach(es -> CollectionUtils.addMapCollection(yearlyStatusUpdates, Integer.valueOf(es.getLevel().getYear()), es));
			for (Map.Entry<Integer, Collection<EliteStatus>> me : yearlyStatusUpdates.entrySet()) {
				Optional<EliteStatus> maxStatus = me.getValue().stream().max(new StatusComparator());
				if (!maxStatus.isEmpty())
					yearMax.put(me.getKey(), maxStatus.get());
			}
			
			// Get the Pilot's history
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			Map<Integer, YearlyTotal> totals = CollectionUtils.createMap(esdao.getEliteTotals(p.getID()), YearlyTotal::getYear);
			totals.putIfAbsent(Integer.valueOf(currentYear), new YearlyTotal(currentYear, p.getID()));
			
			// Save status attributes
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("currentYear", Integer.valueOf(currentYear), REQUEST);
			ctx.setAttribute("baseLevel", EliteLevel.EMPTY, REQUEST);
			ctx.setAttribute("levels", yearlyLevels, REQUEST);
			ctx.setAttribute("currentStatus", currentStatus, REQUEST);
			ctx.setAttribute("totals", totals, REQUEST);
			ctx.setAttribute("maxStatus", yearMax, REQUEST);
			ctx.setAttribute("statusUpdates", yearlyStatusUpdates, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteStatus.jsp");
		result.setSuccess(true);
	}
}
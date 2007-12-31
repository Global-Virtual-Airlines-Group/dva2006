// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display statistics about a Pilot's landings.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class MyFlightStatsCommand extends AbstractStatsCommand {
	
	private static final List DATE_FILTER = ComboUtils.fromArray(new String[] { "All Landings", "30 Days", "60 Days",
		"90 Days" }, new String[] { "0", "30", "60", "90" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);
		
		// Get grouping type
		String labelType = ctx.getParameter("groupType");
		if (StringUtils.arrayIndexOf(GROUP_CODE, labelType) == -1)
			labelType = GROUP_CODE[2];
		else if (GROUP_CODE[6].equals(labelType))
			labelType = MONTH_SQL;
		
		// Get the number of days to retrieve
		int daysBack = StringUtils.parse(ctx.getParameter("days"), 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report statistics
			GetStatistics stdao = new GetStatistics(con);
			stdao.setQueryStart(vc.getStart());
			stdao.setQueryMax(vc.getCount());
			vc.setResults(stdao.getPIREPStatistics(ctx.getUser().getID(), labelType, vc.getSortType(), true));
			
			// Get the DAO and the landing statistics
			GetFlightReportStatistics dao = new GetFlightReportStatistics(con);
			dao.setDayFilter(daysBack);
			Map<Integer, Integer> vsStats = dao.getLandingCounts(ctx.getUser().getID(), 50);
			ctx.setAttribute("vSpeeds", vsStats.keySet(), REQUEST);
			ctx.setAttribute("landingStats", vsStats, REQUEST);
			ctx.setAttribute("eqLandingStats", dao.getLandings(ctx.getUser().getID()), REQUEST);
			
			// Calculate the maximum values for landing range
			int maxLandCount = 0;
			for (Iterator<Integer> i = vsStats.values().iterator(); i.hasNext(); ) {
				Integer totals = i.next();
				maxLandCount = Math.max(maxLandCount, totals.intValue());
			}
			
			// Save maximum count
			ctx.setAttribute("maxCount", new Integer(maxLandCount), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save combobox choices - don't allow grouping by pilot name
		ctx.setAttribute("dateFilter", DATE_FILTER, REQUEST);
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", GROUP_OPTIONS.subList(1, GROUP_OPTIONS.size()), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/myStats.jsp");
		result.setSuccess(true);
	}
}
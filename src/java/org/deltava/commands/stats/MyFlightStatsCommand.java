// Copyright 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display statistics about a Pilot's landings.
 * @author Luke
 * @version 2.8
 * @since 2.1
 */

public class MyFlightStatsCommand extends AbstractStatsCommand {
	
	private static final List<?> DATE_FILTER = ComboUtils.fromArray(new String[] { "All Landings", "30 Days", "60 Days",
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
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		// Get the number of days to retrieve
		int daysBack = StringUtils.parse(ctx.getParameter("days"), 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			// Load legs
			if (p.getACARSLegs() < 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				frdao.getOnlineTotals(p, SystemData.get("airline.db"));
			}
			
			// Get the Flight Report statistics
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			vc.setResults(stdao.getPIREPStatistics(userID, labelType, vc.getSortType(), true));
			
			// Get the DAO and the landing statistics
			stdao.setDayFilter(daysBack);
			Map<Integer, Integer> vsStats = stdao.getLandingCounts(userID, 50);
			ctx.setAttribute("landingStats", vsStats, REQUEST);
			ctx.setAttribute("eqLandingStats", stdao.getLandings(userID), REQUEST);
			
			// Calculate the maximum values for landing range
			int maxLandCount = 0;
			for (Iterator<Integer> i = vsStats.values().iterator(); i.hasNext(); ) {
				Integer totals = i.next();
				maxLandCount = Math.max(maxLandCount, totals.intValue());
			}
			
			// Save maximum count and user
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("maxCount", Integer.valueOf(maxLandCount), REQUEST);
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
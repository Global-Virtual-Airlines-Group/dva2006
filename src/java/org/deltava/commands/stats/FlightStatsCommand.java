// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.beans.stats.FlightStatsEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display sorted Flight Report statistics.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class FlightStatsCommand extends AbstractStatsCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);

		// Get grouping type
		String labelType = ctx.getParameter("groupType");
		if (StringUtils.arrayIndexOf(GROUP_CODE, labelType) == -1)
			labelType = GROUP_CODE[0];
		else if (GROUP_CODE[6].equals(labelType))
			labelType = MONTH_SQL;
		
		try {
			GetFlightReportStatistics dao = new GetFlightReportStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Save the statistics in the request
			List<FlightStatsEntry> results = dao.getPIREPStatistics(0, labelType, vc.getSortType(), true); 
			vc.setResults(results);
			
			// Save pilot ID flag
			boolean hasID = !results.isEmpty() && (results.get(0).getPilotIDs() > 0);
			ctx.setAttribute("hasPilotID", Boolean.valueOf(hasID), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the sorter types in the request
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", GROUP_OPTIONS, REQUEST);

		// Set the result page and return
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
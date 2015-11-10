// Copyright 2005, 2006, 2007, 2008, 2010, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display sorted Flight Report statistics.
 * @author Luke
 * @version 6.2
 * @since 1.0
 */

public class FlightStatsCommand extends AbstractStatsCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);

		// Get grouping type
		String labelType = ctx.getParameter("groupType"); int ofs = StringUtils.arrayIndexOf(GROUP_CODE, labelType);
		if (ofs < 0)
			labelType = GROUP_CODE[0];
		else if (ofs == 6)
			labelType = MONTH_SQL;
		
		try {
			GetAggregateStatistics dao = new GetAggregateStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Save the statistics in the request
			if (labelType.contains("AP."))
				vc.setResults(dao.getAirportStatistics(vc.getSortType(), (ofs - 3)));
			else
				vc.setResults(dao.getPIREPStatistics(labelType, vc.getSortType()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the sorter types in the request
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", GROUP_OPTIONS, REQUEST);
		ctx.setAttribute("hasPilotID", Boolean.valueOf(labelType.contains("P.")), REQUEST);

		// Set the result page and return
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
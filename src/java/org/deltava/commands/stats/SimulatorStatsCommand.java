// Copyright 2008, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display flight simulator version statistics.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class SimulatorStatsCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get grouping / sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class);
		FlightStatsSort srt = FlightStatsSort.from(vc.getSortType(), FlightStatsSort.DATE);
		FlightStatsGroup grp = FlightStatsGroup.from(ctx.getParameter("groupType"), FlightStatsGroup.MONTH);
		if (!grp.isDateGroup() && (srt == FlightStatsSort.DATE)) srt = FlightStatsSort.LEGS;
		vc.setSortType(srt.name()); ctx.setAttribute("groupType", grp, REQUEST);
		
		try {
			GetAggregateStatistics dao = new GetAggregateStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getSimStatistics(srt, grp));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/simVersionStats.jsp");
		result.setSuccess(true);
	}
}
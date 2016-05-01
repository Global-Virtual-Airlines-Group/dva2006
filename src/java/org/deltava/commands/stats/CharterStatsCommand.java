// Copyright 2011, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Charter Flight statistics.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class CharterStatsCommand extends AbstractViewCommand {
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get sorting / grouping
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class);
		FlightStatsSort srt = FlightStatsSort.from(vc.getSortType(), FlightStatsSort.DATE);
		FlightStatsGroup grp = FlightStatsGroup.from(ctx.getParameter("groupType"), FlightStatsGroup.MONTH);
		if (grp.isPilotGroup() && (srt == FlightStatsSort.PIDS)) srt = FlightStatsSort.LEGS;
		vc.setSortType(srt.name());

		try {
			GetFlightReportStatistics dao = new GetFlightReportStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getCharterStatistics(srt, grp));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the sorter types in the request
		ctx.setAttribute("isCharter", Boolean.TRUE, REQUEST);
		ctx.setAttribute("hasPilotID", Boolean.valueOf(!grp.isPilotGroup()), REQUEST);
		ctx.setAttribute("groupType", grp, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
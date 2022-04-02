// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.EnumUtils;

/**
 * A Web Site Command to view online flight statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class OnlineStatsCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get grouping / sorting
		ViewContext<OnlineStatsEntry> vc = initView(ctx, OnlineStatsEntry.class);
		FlightStatsSort srt = EnumUtils.parse(FlightStatsSort.class, vc.getSortType(), FlightStatsSort.DATE);
		FlightStatsGroup grp = EnumUtils.parse(FlightStatsGroup.class, ctx.getParameter("groupType"), FlightStatsGroup.MONTH);
		vc.setSortType(srt.name()); ctx.setAttribute("groupType", grp, REQUEST);
		
		try {
			GetAggregateStatistics stdao = new GetAggregateStatistics(ctx.getConnection());
			stdao.setQueryStart(vc.getStart());
			stdao.setQueryMax(vc.getCount());
			vc.setResults(stdao.getOnlineStatistics(srt, grp));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/onlineStats.jsp");
		result.setSuccess(true);
	}
}
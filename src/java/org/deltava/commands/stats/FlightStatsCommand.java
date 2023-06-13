// Copyright 2005, 2006, 2007, 2008, 2010, 2015, 2016, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.stream.Collectors;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display sorted Flight Report statistics.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class FlightStatsCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get grouping / sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class); 
		FlightStatsSort srt = EnumUtils.parse(FlightStatsSort.class, vc.getSortType(), FlightStatsSort.DATE);
		FlightStatsGroup grp = EnumUtils.parse(FlightStatsGroup.class, ctx.getParameter("groupType"), FlightStatsGroup.MONTH);
		if (!grp.isDateGroup() && (srt == FlightStatsSort.DATE)) srt = FlightStatsSort.LEGS;
		ctx.setAttribute("hasPilotID", Boolean.valueOf(!grp.isPilotGroup() && (!grp.isDateGroup() || (grp == FlightStatsGroup.DATE))), REQUEST);
		ctx.setAttribute("groupType", grp, REQUEST);
		vc.setSortType(srt.name());
		
		try {
			GetAggregateStatistics dao = new GetAggregateStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			if (grp.isAirportGroup())
				vc.setResults(dao.getAirportStatistics(srt, grp.ordinal() - 3));
			else
				vc.setResults(dao.getPIREPStatistics(srt, grp));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort data
		ctx.setAttribute("statSortData", vc.getResults().stream().map(JSONUtils::format).collect(Collectors.toList()), REQUEST);

		// Set the result page and return
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
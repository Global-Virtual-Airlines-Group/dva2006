// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to view Staff flight statistics.
 * @author Luke
 * @version 10.6
 * @since 10.6
 */

public class StaffFlightStatsCommand extends AbstractViewCommand {
	
	private static final List<ComboAlias> DAY_OPTS = ComboUtils.fromArray(new String[] {"30 Days", "60 Days", "90 Days", "120 Days", "150 Days", "180 Days", "365 Days"}, new String[] {"30", "60", "90", "120", "150", "180", "365"});

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get date range
		int days = StringUtils.parse(ctx.getParameter("days"), 90);

		// Get grouping / sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class); 
		FlightStatsSort srt = EnumUtils.parse(FlightStatsSort.class, vc.getSortType(), FlightStatsSort.LEGS);
		vc.setSortType(srt.name());
		ctx.setAttribute("daysBack", Integer.valueOf(days), REQUEST);
		ctx.setAttribute("dayOpts", DAY_OPTS, REQUEST);
		
		try {
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(ctx.getConnection());
			vc.setResults(stdao.getStaffStatistics(days, srt));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/staffFlightStats.jsp");
		result.setSuccess(true);
	}
}
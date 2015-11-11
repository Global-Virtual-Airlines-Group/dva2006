// Copyright 2011, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display Charter Flight statistics.
 * @author Luke
 * @version 6.3
 * @since 3.6
 */

public class CharterStatsCommand extends AbstractStatsCommand {
	
	/**
	 * Group option SQL.
	 */
	private static final String[] CH_GROUP_CODE = {"CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "F.DATE", "F.EQTYPE",
			"$MONTH", "DATE_SUB(F.DATE, INTERVAL WEEKDAY(F.DATE) DAY)", "YEAR(F.DATE)"};
	
	/**
	 * Group option labels.
	 */
	private static final List<?> CH_GROUP_OPTS = ComboUtils.fromArray(new String[] {"Pilot Name", "Flight Date", "Equipment Type", 
			"Month", "Week", "Year" }, CH_GROUP_CODE);
	
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
		String labelType = CH_GROUP_CODE[StringUtils.arrayIndexOf(CH_GROUP_CODE, ctx.getParameter("groupType"), 0)];
		if (CH_GROUP_CODE[3].equals(labelType))
			labelType = MONTH_SQL;

		try {
			GetFlightReportStatistics dao = new GetFlightReportStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getCharterStatistics(labelType, vc.getSortType()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the sorter types in the request
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", CH_GROUP_OPTS, REQUEST);
		ctx.setAttribute("isCharter", Boolean.TRUE, REQUEST);
		ctx.setAttribute("hasPilotID", Boolean.valueOf(labelType.contains("P.")), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
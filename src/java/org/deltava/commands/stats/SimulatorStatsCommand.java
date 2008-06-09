// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display flight simulator version statistics.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class SimulatorStatsCommand extends AbstractViewCommand {

	/**
	 * Sort option SQL.
	 */
	private static final String[] SORT_CODE = { "LEGS", "HOURS", "MILES", "F.DATE" };

	/**
	 * Sort option labels.
	 */
	private static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] { "Flight Legs", "Miles Flown",
			"Flight Hours", "Flight Date" }, SORT_CODE);

	/**
	 * Group option SQL.
	 */
	private static final String[] GROUP_CODE = { "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "F.DATE", "F.EQTYPE",
			"$MONTH", "DATE_SUB(F.DATE, INTERVAL WEEKDAY(F.DATE) DAY)" };

	/**
	 * Group option labels.
	 */
	private static final List GROUP_OPTIONS = ComboUtils.fromArray(new String[] { "Pilot Name", "Flight Date",
			"Equipment Type", "Month", "Week" }, GROUP_CODE);

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
		else if (GROUP_CODE[3].equals(labelType))
			labelType = AbstractStatsCommand.MONTH_SQL;

		try {
			GetFlightReportStatistics dao = new GetFlightReportStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Save the statistics in the request
			vc.setResults(dao.getSimStatistics(labelType, vc.getSortType() + " DESC"));
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
		result.setURL("/jsp/stats/simVersionStats.jsp");
		result.setSuccess(true);
	}
}
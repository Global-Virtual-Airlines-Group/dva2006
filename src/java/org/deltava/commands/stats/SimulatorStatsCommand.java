// Copyright 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display flight simulator version statistics.
 * @author Luke
 * @version 6.2
 * @since 2.2
 */

public class SimulatorStatsCommand extends AbstractViewCommand {

	private static final String[] SORT_CODE = {"DATE DESC", "SL DESC", "SM DESC", "SH DESC"};
	private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Flight Date", "Flight Legs", "Distance Flown", "Flight Hours"}, SORT_CODE);
	
	private static final String[] GROUP_CODE = { "$MONTH", "DATE_SUB(DATE, INTERVAL WEEKDAY(DATE) DAY)", "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "EQTYPE"};
	private static final List<?> GROUP_OPTIONS = ComboUtils.fromArray(new String[] {"Month", "Week", "Pilot Name", "Equipment Type"}, GROUP_CODE);

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get grouping / sorting
		ViewContext vc = initView(ctx); 
		int sortIdx = Math.max(0, StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()));
		String labelType = ctx.getParameter("groupType"); int idx = StringUtils.arrayIndexOf(GROUP_CODE, labelType); 
		if (idx < 1)
			labelType = AbstractStatsCommand.MONTH_SQL;
		else if ((idx > 1) && (sortIdx == 0))
			sortIdx = 1;

		try {
			GetAggregateStatistics dao = new GetAggregateStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setSortType(SORT_CODE[sortIdx]);
			vc.setResults(dao.getSimStatistics(labelType, vc.getSortType()));
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
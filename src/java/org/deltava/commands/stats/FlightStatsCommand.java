// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display sorted Flight Report statistics.
 * @author Luke
 * @version 2.1
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
			Connection con = ctx.getConnection();

			// Get the DAO
			GetStatistics dao = new GetStatistics(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Save the statistics in the request
			vc.setResults(dao.getPIREPStatistics(0, labelType, vc.getSortType(), true));
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
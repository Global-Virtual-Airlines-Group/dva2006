// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.stats.EventStats;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display Online Event statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class EventStatsCommand extends AbstractViewCommand {
	
	private static final List<ComboAlias> GROUP_OPTS = ComboUtils.fromArray("Individual", "Monthly");

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<EventStats> vctx = initView(ctx, EventStats.class);
		boolean isMonthly = "monthly".equalsIgnoreCase(String.valueOf(ctx.getCmdParameter(Command.OPERATION, "monthly")));
		try {
			GetEventStatistics stdao = new GetEventStatistics(ctx.getConnection());
			stdao.setQueryStart(vctx.getStart());
			stdao.setQueryMax(vctx.getCount());
			vctx.setResults(isMonthly ? stdao.getMonthlyStatistics() : stdao.getStatistics());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("groupTypes", GROUP_OPTS, REQUEST);
		ctx.setAttribute("isMonthly", Boolean.valueOf(isMonthly), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/eventStats.jsp");
		result.setSuccess(true);
	}
}
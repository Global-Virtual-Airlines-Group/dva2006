// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import org.deltava.beans.testing.ExamStatsEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Examination and Check Ride statistics. 
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class ExamStatsCommand extends AbstractViewCommand {
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view Context
		ViewContext vc = initView(ctx);
		boolean isCR = "cr".equals(ctx.getParameter("isCR"));
		
		try {
			GetExamStatistics dao = new GetExamStatistics(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Do the query
			if (isCR)
				vc.setResults(dao.getCheckrideStatistics(label, subLabel));
			else
				vc.setResults(dao.getExamStatistics(label, subLabel));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save sort/group options
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/examStats.jsp");
		result.setSuccess(true);
	}
}
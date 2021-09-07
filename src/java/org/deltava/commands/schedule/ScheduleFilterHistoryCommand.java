// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Collection;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.schedule.ScheduleSourceHistory;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display schedule filter history. 
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public class ScheduleFilterHistoryCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<ScheduleSourceHistory> vc = initView(ctx, ScheduleSourceHistory.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the history
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			sidao.setQueryStart(vc.getStart());
			sidao.setQueryMax(vc.getCount());
			vc.setResults(sidao.getHistory());
			
			// Load Pilots if needed
			Collection<Integer> IDs = vc.getResults().stream().filter(ssh -> (ssh.getAuthorID() != 0)).map(ScheduleSourceHistory::getAuthorID).collect(Collectors.toSet());
			if (!IDs.isEmpty()) {
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/filterHistory.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.beans.schedule.Hub;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display a list of Airline Hubs.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class HubListCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext<Hub> vctx = initView(ctx, Hub.class);
		try {
			GetRawScheduleInfo dao = new GetRawScheduleInfo(ctx.getConnection());
			dao.setQueryStart(vctx.getStart());
			dao.setQueryMax(vctx.getCount());
			vctx.setResults(dao.getHubs());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/hubList.jsp");
		result.setSuccess(true);
	}
}
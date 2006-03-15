// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.Map;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display home page referer statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RefererStatsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the start/view/count
		ViewContext vc = initView(ctx, 150);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the statistics
			GetSystemData dao = new GetSystemData(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Map<String, Long> results = dao.getReferers();
			
			// Save in the requests 
			vc.setResults(results.keySet());
			ctx.setAttribute("stats", results, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/homeReferers.jsp");
		result.setSuccess(true);
	}
}
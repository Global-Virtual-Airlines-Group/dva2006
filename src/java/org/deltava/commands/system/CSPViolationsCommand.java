// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.beans.system.CSPViolations;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display CSP violation statistics.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class CSPViolationsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<CSPViolations> vc = initView(ctx, CSPViolations.class);
		try {
			GetBrowserReports dao = new GetBrowserReports(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getStatistics());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/cspViolations.jsp");
		result.setSuccess(true);
	}
}
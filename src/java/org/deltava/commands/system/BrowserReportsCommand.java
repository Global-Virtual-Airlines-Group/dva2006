// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.beans.system.BrowserReport;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Reporting API browser reports.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class BrowserReportsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		String url = ctx.getParameter("id");
		ViewContext<BrowserReport> vc = initView(ctx, BrowserReport.class);
		try {
			GetBrowserReports dao = new GetBrowserReports(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(StringUtils.isEmpty(url) ? dao.getBrowserReports() : dao.getReportsByURL(url));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/browserReports.jsp");
		result.setSuccess(true);
	}
}
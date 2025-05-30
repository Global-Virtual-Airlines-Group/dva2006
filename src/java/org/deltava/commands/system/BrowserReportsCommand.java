// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.AuthoredBean;
import org.deltava.beans.system.BrowserReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

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
		
		ViewContext<BrowserReport> vc = initView(ctx, BrowserReport.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the reports
			GetSystemData dao = new GetSystemData(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getBrowserReports());
			
			// Get the authors
			Collection<Integer> authorIDs = vc.getResults().stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/system/browserReports.jsp");
		result.setSuccess(true);
	}
}
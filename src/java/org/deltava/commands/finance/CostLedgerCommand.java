// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.util.List;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.finance.CostEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display the cost ledger. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class CostLedgerCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get view context and year
		LocalDate ld = LocalDate.now();
		ViewContext<CostEntry> vctx = initView(ctx, CostEntry.class);
		int y = StringUtils.parse(ctx.getParameter("year"), ld.getYear());
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the ledger entries
			GetFinance fdao = new GetFinance(con);
			fdao.setQueryStart(vctx.getStart());
			fdao.setQueryMax(vctx.getCount());
			vctx.setResults(fdao.getCostLedger(y));
			
			// Get the author names
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			Collection<Integer> IDs = vctx.getResults().stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save view options
		ctx.setAttribute("years", List.of(Integer.valueOf(ld.getYear()), Integer.valueOf(ld.getYear() - 1)), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/costLedger.jsp");
		result.setSuccess(true);
	}
}
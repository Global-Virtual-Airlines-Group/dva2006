// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.finance.SharePurchase;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view the shareholder ledger. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class ShareLedgerCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the year and the view context
		ViewContext<SharePurchase> vctx = initView(ctx, SharePurchase.class);
		int y = StringUtils.parse(ctx.getParameter("year"), LocalDate.now().getYear());
		try {
			Connection con = ctx.getConnection();
			GetFinance fdao = new GetFinance(con);
			fdao.setQueryStart(vctx.getStart());
			fdao.setQueryMax(vctx.getCount());
			vctx.setResults(fdao.getShares(y));
			
			// Get the owners
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
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/shareLedger.jsp");
		result.setSuccess(true);
	}
}
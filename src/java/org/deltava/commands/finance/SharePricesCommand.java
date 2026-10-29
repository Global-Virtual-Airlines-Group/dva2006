// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import org.deltava.beans.finance.SharePrice;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display current and historical share prices. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class SharePricesCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<SharePrice> vctx = initView(ctx, SharePrice.class);
		try {
			GetFinance fdao = new GetFinance(ctx.getConnection());
			vctx.setResults(fdao.getSharePrices());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/sharePrices.jsp");
		result.setSuccess(true);
	}
}
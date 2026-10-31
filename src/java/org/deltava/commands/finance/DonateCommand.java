// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.finance.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to handle share purchases.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class DonateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Check for an active year
			GetFinance fdao = new GetFinance(con);
			SharePrice sp = fdao.getSharePrices().stream().filter(SharePrice::getActive).findAny().orElse(null);
			if (sp == null)
				throw new CommandException("No Active Year for donations", false);
			
			// Get if we've donated already
			Collection<SharePurchase> purchases = fdao.getSharePurchases(ctx.getUser().getID());
			if (purchases.stream().anyMatch(s -> (s.getYear() == sp.getYear())))
				throw new CommandException(String.format("%s has already donated for %d", ctx.getUser().getName(), Integer.valueOf(sp.getYear())), false);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/donate.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.time.*;
import java.util.List;

import org.deltava.beans.finance.AWSCostEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display AWS costs.
 * @author Luke
 * @version 13.1
 * @since 13.1
 */

public class AWSLedgerCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get month and view context
		LocalDate ld = LocalDate.now();
		ViewContext<AWSCostEntry> vctx = initView(ctx, AWSCostEntry.class);
		int y = StringUtils.parse(ctx.getParameter("year"), ld.getYear());
		int m = StringUtils.parse(ctx.getParameter("month"), ld.getMonthValue());
		
		try {
			GetFinance dao = new GetFinance(ctx.getConnection());
			dao.setQueryStart(vctx.getStart());
			dao.setQueryMax(vctx.getCount());
			vctx.setResults(dao.getAWSCosts(y, m));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get options
		ctx.setAttribute("years", List.of(Integer.valueOf(ld.getYear()), Integer.valueOf(ld.getYear() - 1)), REQUEST);
		ctx.setAttribute("months", ComboUtils.properCase(Month.values()), REQUEST);
		ctx.setAttribute("isAWS", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/costLedger.jsp");
		result.setSuccess(true);
	}
}
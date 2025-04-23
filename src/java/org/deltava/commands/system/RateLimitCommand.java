// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import static org.deltava.commands.HTTPContext.RTLIMIT_ATTR_NAME;

import java.util.*;

import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.RequestCounterComparator;

/**
 * A Web Site Command to view HTTP request rate limiter status. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RateLimitCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get sort options
		Comparator<RequestCounter> cmp = new RequestCounterComparator(RequestCounterComparator.REQUESTS).reversed();
		
		// Get the rate limiter
		ViewContext<RequestCounter> vc = initView(ctx, RequestCounter.class);
		RateLimiter rl = (RateLimiter) ctx.getRequest().getServletContext().getAttribute(RTLIMIT_ATTR_NAME);
		List<RequestCounter> results = rl.getCounters();
		results.sort(cmp);
		if (vc.getStart() <= results.size())
			vc.setResults(results.subList(vc.getStart(), Math.min(results.size(), vc.getStart() + vc.getCount())));
		
		// Load netblocks
		try {
			GetIPLocation ipdao = new GetIPLocation(ctx.getConnection());
			for (RequestCounter rc : vc.getResults()) {
				if (rc.getIPInfo() != null) continue;
				IPBlock ip = ipdao.get(rc.getAddress());
				rc.setIPInfo(ip);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/rateLimit.jsp");
		result.setSuccess(true);
	}
}
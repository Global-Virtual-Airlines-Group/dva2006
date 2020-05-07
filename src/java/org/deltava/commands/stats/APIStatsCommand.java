// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.deltava.beans.stats.*;
import org.deltava.beans.system.API;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view external API usage statistics. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class APIStatsCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int daysBack = StringUtils.parse(ctx.getParameter("days"), 90);
		try {
			Map<API, Collection<APIUsage>> usage = new HashMap<API, Collection<APIUsage>>();
			Map<String, APIUsage> predictions = new HashMap<String, APIUsage>();
			GetSystemLog dao = new GetSystemLog(ctx.getConnection());
			for (API api : API.values()) {
				Collection<APIUsage> stats = dao.getAPIRequests(api, daysBack);
				Collection<String> methodNames = stats.stream().map(APIUsage::getName).collect(Collectors.toCollection(TreeSet::new));
				methodNames.forEach(name -> predictions.put(name, APIUsageHelper.predictUsage(stats, name)));
				usage.put(api, stats);
			}
			
			// Save in request
			ctx.setAttribute("usage", usage, REQUEST);
			ctx.setAttribute("predict", predictions, REQUEST);
			ctx.setAttribute("daysBack", Integer.valueOf(daysBack), REQUEST);
			ctx.setAttribute("daysRemaining", Integer.valueOf(APIUsageHelper.getDaysLeftInMonth()), REQUEST);
			ctx.setAttribute("startDate", LocalDate.now().minusDays(daysBack), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/apiUsage.jsp");
		result.setSuccess(true);
	}
}
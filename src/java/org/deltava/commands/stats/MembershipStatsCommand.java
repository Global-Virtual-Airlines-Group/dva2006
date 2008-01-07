// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display membership statistics.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class MembershipStatsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get quantiles
		int quantiles = StringUtils.parse(ctx.getParameter("quantiles"), 10);
		
		Map<Integer, Date> qResults = null;
		Collection<MembershipTotals> joinDates = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the statistics
			GetStatistics dao = new GetStatistics(con);
			ctx.setAttribute("totals", dao.getAirlineTotals(), REQUEST);
			joinDates = dao.getJoinStats();
			qResults = dao.getMembershipQuantiles(quantiles);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate the maximum values for periods
		int maxJoinCount = 0;
		for (Iterator<MembershipTotals> i = joinDates.iterator(); i.hasNext(); ) {
			MembershipTotals totals = i.next();
			maxJoinCount = Math.max(maxJoinCount, totals.getCount());
		}

		// Save join statistics
		ctx.setAttribute("joinDates", joinDates, REQUEST);
		ctx.setAttribute("maxCount", new Integer(maxJoinCount), REQUEST);
		
		// Save quantiles
		ctx.setAttribute("quantiles", qResults, REQUEST);
		ctx.setAttribute("quantileCount", new Integer(quantiles), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/membership.jsp");
		result.setSuccess(true);
	}
}
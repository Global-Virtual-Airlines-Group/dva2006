// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.stats.DispatchStatistics;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display ACARS Dispatcher statistics.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class DispatcherStatsCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the date
		DateRange dr = DateRange.parse(ctx.getParameter("range"));
		if (dr == null)
			dr = DateRange.createMonth(Instant.now());
		
		try {
			Connection con = ctx.getConnection();
			
			// Load the statistics
			GetACARSDispatchStats sdao = new GetACARSDispatchStats(con);
			Collection<DispatchStatistics> stats = sdao.getTopDispatchers(dr);
			ctx.setAttribute("stats", stats, REQUEST);
			ctx.setAttribute("ranges", sdao.getDispatchRanges(), REQUEST);
			
			// Get the User IDs
			Collection<Integer> IDs = stats.stream().map(DispatchStatistics::getID).collect(Collectors.toSet());
			
			// Load the users
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate the length - if it's the current time period adjust accordingly
		Instant now = Instant.now();
		if (dr.getEndDate().isAfter(now))
			dr = new DateRange(dr.getStartDate(), now);
		
		// Display dates
		ctx.setAttribute("range", dr, REQUEST);
		ctx.setAttribute("totalHours", new Double(dr.getLength() / 3600000.0), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/topDispatchers.jsp");
		result.setSuccess(true);
	}
}
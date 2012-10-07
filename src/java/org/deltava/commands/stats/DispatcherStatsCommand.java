// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.stats.DispatchStatistics;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CalendarUtils;

/**
 * A Web Site Command to display ACARS Dispatcher statistics.
 * @author Luke
 * @version 5.0
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
		if (dr == null) {
			Calendar cld = CalendarUtils.getInstance(null, true);
			cld.set(Calendar.DAY_OF_MONTH, 1);
			dr = DateRange.createMonth(cld.getTime());
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Load the statistics
			GetACARSDispatchStats sdao = new GetACARSDispatchStats(con);
			Collection<DispatchStatistics> stats = sdao.getTopDispatchers(dr);
			ctx.setAttribute("stats", stats, REQUEST);
			ctx.setAttribute("ranges", sdao.getDispatchRanges(), REQUEST);
			
			// Get the User IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (DispatchStatistics st : stats)
				IDs.add(Integer.valueOf(st.getID()));
			
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
		Date now = new Date();
		if (dr.getEndDate().after(now))
			dr = new DateRange(dr.getStartDate(), now);
		
		// Display dates
		ctx.setAttribute("utc", TZInfo.UTC, REQUEST);
		ctx.setAttribute("range", dr, REQUEST);
		ctx.setAttribute("totalHours", new Double(dr.getLength() / 3600000.0), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/topDispatchers.jsp");
		result.setSuccess(true);
	}
}
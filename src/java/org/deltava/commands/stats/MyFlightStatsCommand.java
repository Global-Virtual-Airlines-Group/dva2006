// Copyright 2007, 2008, 2009, 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display statistics about a Pilot's landings.
 * @author Luke
 * @version 7.0
 * @since 2.1
 */

public class MyFlightStatsCommand extends AbstractViewCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get grouping / sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class);
		FlightStatsSort srt = FlightStatsSort.from(vc.getSortType(), FlightStatsSort.LEGS);
		FlightStatsGroup grp = FlightStatsGroup.from(ctx.getParameter("groupType"), FlightStatsGroup.EQ);
		vc.setSortType(srt.name()); ctx.setAttribute("groupType", grp, REQUEST);

		// Get the user ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		// Get the number of days to retrieve
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			// Load legs
			if (p.getACARSLegs() < 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				frdao.getOnlineTotals(p, SystemData.get("airline.db"));
			}
			
			// Get the Flight Report statistics
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			vc.setResults(stdao.getPIREPStatistics(userID, srt, grp));
			
			// Get the DAO and the landing statistics
			ctx.setAttribute("eqLandingStats", stdao.getLandings(userID), REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/myStats.jsp");
		result.setSuccess(true);
	}
}
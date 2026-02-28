// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.stats.RunwayLandingStats;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to list Runways with the lowest aggregate landing scores.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class ChallengingRunwaysCommand extends AbstractViewCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<RunwayLandingStats> vc = initView(ctx, RunwayLandingStats.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the statistics DAO
			GetAggregateStatistics sdao = new GetAggregateStatistics(con);
			sdao.setQueryStart(vc.getStart());
			sdao.setQueryMax(vc.getCount());
			
			// Load the runway names
			vc.setResults(sdao.getChalleningRunways(10));
			
			// Populate runway data
			GetNavData navdao = new GetNavData(con);
			Map<String, Runway> rwyData = new HashMap<String, Runway>();
			for (RunwayLandingStats rwy : vc.getResults()) {
				Runway r = navdao.getRunway(rwy.getAirport(), rwy.getRunway(), Simulator.UNKNOWN);
				if (r != null)
					rwyData.put(String.format("%s-%s", r.getCode(), rwy.getRunway()), r);
			}
			
			ctx.setAttribute("rwys", rwyData, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/challengingRunways.jsp");
		result.setSuccess(true);
	}
}
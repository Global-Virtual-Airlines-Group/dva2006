// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.stats.LandingStatistics;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display landing statistics.
 * @author Luke
 * @version 2.6
 * @since 2.1
 */

public class LandingStatsCommand extends AbstractViewCommand {
	
	private static final List<?> DATE_FILTER = ComboUtils.fromArray(new String[] { "All Landings", "30 Days", "60 Days",
		"90 Days" }, new String[] { "0", "30", "60", "90" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Load the view context and minimum landings
		ViewContext vc = initView(ctx, 25);
		int minLegs = Math.max(1, StringUtils.parse(ctx.getParameter("legCount"), 20));
		ctx.setAttribute("legCount", Integer.valueOf(minLegs), REQUEST);
		
		// Check equipment type
		String eqType = ctx.getParameter("eqType");
		if (StringUtils.isEmpty(eqType))
			eqType = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the results
			GetFlightReportStatistics dao = new GetFlightReportStatistics(con);
			dao.setDayFilter(StringUtils.parse(ctx.getParameter("days"), 30));
			dao.setQueryMax(vc.getCount());
			Collection<LandingStatistics> stats = dao.getLandings(eqType, minLegs);
			vc.setResults(stats);
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (LandingStatistics ls : stats)
				IDs.add(new Integer(ls.getID()));
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Save equipment choices
			GetFlightReportRecognition rdao = new GetFlightReportRecognition(con); 
			List<Object> eqTypes = new ArrayList<Object>();
			eqTypes.add(ComboUtils.fromString("All Aircraft", ""));
			eqTypes.addAll(rdao.getACARSEquipmentTypes(minLegs));
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save combobox choices
		ctx.setAttribute("dateFilter", DATE_FILTER, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/landingStats.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2007, 2009, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.stats.LandingStatistics;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display landing statistics.
 * @author Luke
 * @version 8.3
 * @since 2.1
 */

public class LandingStatsCommand extends AbstractViewCommand {
	
	private static final List<?> DATE_FILTER = ComboUtils.fromArray(new String[] { "All Landings", "30 Days", "60 Days", "90 Days", "180 Days", "365 Days" }, new String[] { "0", "30", "60", "90", "180", "365" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Load the view context and minimum landings
		ViewContext<LandingStatistics> vc = initView(ctx, LandingStatistics.class, 50);
		int minLegs = Math.max(0, StringUtils.parse(ctx.getParameter("legCount"), 200));
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
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getLandings(eqType, minLegs));
			
			// Load the Pilots
			Collection<Integer>IDs = vc.getResults().stream().map(LandingStatistics::getID).collect(Collectors.toSet());
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
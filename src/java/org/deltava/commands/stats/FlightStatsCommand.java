// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetStatistics;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display sorted Flight Report statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightStatsCommand extends AbstractViewCommand {

	// Sort by options
	private static final String[] SORT_CODE = {"LEGS", "MILES", "HOURS", "AVGHOURS", "AVGMILES", "F.DATE", "ACARSLEGS",
		"OLEGS", "HISTLEGS"};
	private static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Flight Legs", "Miles Flown", "Flight Hours", 
			"Avg. Hours", "Avg. Miles", "Flight Date", "ACARS Legs", "Online Legs", "Historic Legs"}, SORT_CODE);

	// Group by options
	private static final String[] GROUP_CODE = {"CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "F.DATE", "F.EQTYPE",
			"AL.NAME", "AP.AIRPORT_D", "AP.AIRPORT_A", "$MONTH", "DATE_SUB(F.DATE, INTERVAL WEEKDAY(F.DATE) DAY)" };
	private static final List GROUP_OPTIONS = ComboUtils.fromArray(new String[] {"Pilot Name", "Flight Date", "Equipment Type", 
			"Airline", "Departed from", "Arrived at", "Month", "Week" }, GROUP_CODE);

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);

		// Get grouping type
		String labelType = ctx.getParameter("groupType");
		if (StringUtils.arrayIndexOf(GROUP_CODE, labelType) == -1)
			labelType = GROUP_CODE[0];
		else if (GROUP_CODE[6].equals(labelType))
			labelType = "DATE_FORMAT(F.DATE, '%M %x')";
		
		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetStatistics dao = new GetStatistics(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Save the statistics in the request
			vc.setResults(dao.getPIREPStatistics(labelType, vc.getSortType(), true));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the sorter types in the request
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", GROUP_OPTIONS, REQUEST);

		// Set the result page and return
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/flightStats.jsp");
		result.setSuccess(true);
	}
}
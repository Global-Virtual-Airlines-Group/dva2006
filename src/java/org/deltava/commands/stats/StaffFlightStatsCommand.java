// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to view Staff flight statistics.
 * @author Luke
 * @version 10.6
 * @since 10.6
 */

public class StaffFlightStatsCommand extends AbstractViewCommand {
	
	private static final List<ComboAlias> DAY_OPTS = ComboUtils.fromArray(new String[] {"30 Days", "60 Days", "90 Days", "120 Days", "150 Days", "180 Days", "365 Days"}, new String[] {"30", "60", "90", "120", "150", "180", "365"});

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get date range
		int days = StringUtils.parse(ctx.getParameter("days"), 90);

		// Get sorting
		ViewContext<FlightStatsEntry> vc = initView(ctx, FlightStatsEntry.class); 
		FlightStatsSort srt = EnumUtils.parse(FlightStatsSort.class, vc.getSortType(), FlightStatsSort.LEGS);
		vc.setSortType(srt.name());
		ctx.setAttribute("daysBack", Integer.valueOf(days), REQUEST);
		ctx.setAttribute("dayOpts", DAY_OPTS, REQUEST);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get staff profiles
			GetStaff sdao = new GetStaff(con);
			Collection<Integer> staffIDs = sdao.getStaff().stream().map(Staff::getID).collect(Collectors.toSet());
			
			// Load Chief Pilots and Assistants
			Collection<Pilot> pilots = new HashSet<Pilot>();
			GetPilot pdao = new GetPilot(con);
			pilots.addAll(pdao.getPilotsByRank(Rank.CP));
			pilots.addAll(pdao.getPilotsByRank(Rank.ACP));
			pilots.addAll(pdao.getByID(staffIDs, "PILOTS").values());
			Collection<Integer> IDs = pilots.stream().map(Pilot::getID).collect(Collectors.toSet());
			
			// Load stats
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			Collection<FlightStatsEntry> stats = stdao.getStatistics(IDs, days, srt);
			
			// Find missing staff members
			Collection<String> pilotNames = stats.stream().map(FlightStatsEntry::getLabel).collect(Collectors.toSet());
			pilots.removeIf(p -> pilotNames.contains(p.getName()));

			// Add stats for missing members
			pilots.stream().map(p -> new FlightStatsEntry(p.getName(), 0, 0, 0)).forEach(stats::add);
			vc.setResults(stats);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/staffFlightStats.jsp");
		result.setSuccess(true);
	}
}
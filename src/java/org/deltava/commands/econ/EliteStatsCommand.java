// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.econ.EliteLevel;
import org.deltava.beans.econ.EliteScorer;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view statistics about the Elite program.
 * @author Luke
 * @version 11.0
 * @since 9.2
 */

public class EliteStatsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int year = EliteScorer.getStatusYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Load current levels
			GetElite eldao = new GetElite(con);
			SortedSet<EliteLevel> lvls = eldao.getLevels(year);
			lvls.remove(lvls.first()); // remove dummy "Member"
			ctx.setAttribute("cyLevels", lvls, REQUEST);
			
			// Load stats
			GetEliteStatistics elsdao = new GetEliteStatistics(con);
			List<ElitePercentile> pcts = elsdao.getElitePercentiles(year, 1);
			ctx.setAttribute("elitePcts", pcts, REQUEST);
			
			// Get prediction of next year
			TreeSet<EliteLevel> nyLevels = eldao.getLevels(year + 1);
			if (nyLevels.isEmpty()) {
				GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);

				// Go one year back
				LocalDate sd = LocalDate.now().minusMonths(12).minusDays(LocalDate.now().getDayOfMonth() - 1);
				ctx.setAttribute("estimateStart", sd, REQUEST);
				ctx.setAttribute("estimatedLevels", Boolean.TRUE, REQUEST);
				
				PercentileStatsEntry lpse = frsdao.getFlightPercentiles(sd, 1, "LEGS, DST");
				PercentileStatsEntry dpse = frsdao.getFlightPercentiles(sd, 1, "DST, LEGS");
				for (EliteLevel ol : lvls) {
					EliteLevel nl = new EliteLevel(ol.getYear() + 1, ol.getName(), ctx.getDB());
					nl.setColor(ol.getColor());
					nl.setBonusFactor(ol.getBonusFactor());
					nl.setTargetPercentile(ol.getTargetPercentile());
					nl.setStatisticsStartDate(sd.atStartOfDay().toInstant(ZoneOffset.UTC));
					nl.setLegs(EliteLevel.round(lpse.getLegs(nl.getTargetPercentile()), SystemData.getInt("econ.elite.round.leg", 5)));
					nl.setDistance(EliteLevel.round(dpse.getDistance(nl.getTargetPercentile()), SystemData.getInt("econ.elite.round.distance", 5000)));
					nyLevels.add(nl);
				}
			}
			
			ctx.setAttribute("nyLevels", nyLevels, REQUEST);
			ctx.setAttribute("currentYear", Integer.valueOf(year), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteStats.jsp");
		result.setSuccess(true);
	}
}
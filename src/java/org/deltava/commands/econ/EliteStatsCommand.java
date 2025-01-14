// Copyright 2020, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view statistics about the Elite program.
 * @author Luke
 * @version 11.5
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
		
		final int year = EliteScorer.getStatusYear(Instant.now());
		LocalDate startDate = LocalDate.of(year, 1, 1);
		try {
			Connection con = ctx.getConnection();
			
			// Check for completed rollover period
			GetEliteStatistics elsdao = new GetEliteStatistics(con);
			boolean doNextYear = EliteScorer.isRollover() && elsdao.isRolloverComplete(year + 1);
			ctx.setAttribute("statsYear", Integer.valueOf(year + (doNextYear ? 1 : 0)), REQUEST);
			
			// Load current levels
			GetElite eldao = new GetElite(con);
			SortedSet<EliteLevel> lvls = eldao.getLevels(year);
			lvls.removeFirst(); // remove dummy "Member"
			ctx.setAttribute("cyLevels", lvls, REQUEST);
			
			// Check if we're in the rollover period
			boolean isRollover = EliteScorer.isRollover();
			ctx.setAttribute("isRollover", Boolean.valueOf(isRollover), REQUEST);
			
			// Load elite stats
			List<YearlyTotal> eTotals = elsdao.getPilotTotals(startDate);
			FlightPercentileHelper eHelper = new FlightPercentileHelper(eTotals, 1);
			ctx.setAttribute("elpse", eHelper.getLegs(), REQUEST);
			ctx.setAttribute("edpse", eHelper.getDistance(), REQUEST);
			ctx.setAttribute("eppse", eHelper.getPoints(), REQUEST);

			// Load flight stats from one year back
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			LocalDate sd = isRollover ? startDate : LocalDate.now().minusMonths(12).minusDays(LocalDate.now().getDayOfMonth() - 1);
			List<YearlyTotal> fTotals = frsdao.getPilotTotals(sd);
			FlightPercentileHelper fHelper = new FlightPercentileHelper(fTotals, 1);
			
			// Get prediction of next year
			TreeSet<EliteLevel> nyLevels = eldao.getLevels(year + 1);
			if (nyLevels.isEmpty()) {
				ctx.setAttribute("estimateStart", sd, REQUEST);
				ctx.setAttribute("estimateEnd", sd.plusYears(1), REQUEST);
				ctx.setAttribute("estimatedLevels", Boolean.TRUE, REQUEST);

				// Map to levels/perenctiles
				PercentileStatsEntry lpse = fHelper.getLegs(); PercentileStatsEntry dpse = fHelper.getDistance(); PercentileStatsEntry ppse = fHelper.getPoints();
				for (EliteLevel ol : lvls) {
					EliteLevel nl = new EliteLevel(ol.getYear() + 1, ol.getName(), ctx.getDB());
					nl.setColor(ol.getColor());
					nl.setBonusFactor(ol.getBonusFactor());
					nl.setTargetPercentile(ol.getTargetPercentile());
					nl.setStatisticsStartDate(sd.atStartOfDay().toInstant(ZoneOffset.UTC));
					nl.setLegs(EliteLevel.round(lpse.getLegs(nl.getTargetPercentile()), SystemData.getInt("econ.elite.round.leg", 5)));
					nl.setDistance(EliteLevel.round(dpse.getDistance(nl.getTargetPercentile()), SystemData.getInt("econ.elite.round.distance", 5000)));
					nl.setPoints(EliteLevel.round(ppse.getPoints(nl.getTargetPercentile()), SystemData.getInt("econ.elite.round.points", 5000)));
					nyLevels.add(nl);
				}
			} else
				nyLevels.removeFirst(); // remove dummy "Member"
			
			// Save flight percentiles
			ctx.setAttribute("flpse", fHelper.getLegs(), REQUEST);
			ctx.setAttribute("fdpse", fHelper.getDistance(), REQUEST);
			
			// Map percentiles to targets
			ctx.setAttribute("targetLvls", CollectionUtils.createMap(nyLevels, EliteLevel::getTargetPercentile), REQUEST);
			
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
// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.PercentileStatsEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to automatically calulate Elite levels for an upcoming year.
 * @author Luke
 * @version 11.0
 * @since 9.2
 */

public class EliteLevelSetCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		EliteAccessControl ac = new EliteAccessControl(ctx);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException("Cannot calculate Elite levels");
		
		// Get the stats start date
		ZonedDateTime now = ZonedDateTime.now();
		 int currentYear = EliteScorer.getStatusYear(now.toInstant());
		 ctx.setAttribute("startDate", ZonedDateTime.of(currentYear - 1, 12, 1, 12, 0, 0, 0, ZoneOffset.UTC), REQUEST);
		 ctx.setAttribute("year", Integer.valueOf(currentYear + 1), REQUEST);
		
		// Redirect to JSP if needed
		CommandResult result = ctx.getResult();
		Instant startDate = parseDateTime(ctx, "start");
		if (startDate == null) {
			result.setURL("/jsp/econ/eliteLevelSet.jsp");
			result.setSuccess(true);
			return;
		}
		
		// If we're calculating statistics for less than the previous calendar year
		ZonedDateTime zsd = ZonedDateTime.ofInstant(startDate, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
		float factor = 1.0f;
		if (zsd.isAfter(now.minusYears(1)))  {
			Duration d = Duration.between(zsd, now);
			int daysInYear = Year.isLeap(now.getYear()) ? 365 : 366;
			factor = daysInYear * 24f / d.toHours();
		}
		  
		boolean isCommit = Boolean.parseBoolean(ctx.getParameter("doCommit"));
		try {
			Connection con = ctx.getConnection();
			
			// Get current year's levels
			GetElite edao = new GetElite(con);
			Collection<EliteLevel> cyLevels = edao.getLevels(currentYear) ;
			
			// Get the PIREP statistics for the year
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			PercentileStatsEntry lst = stdao.getFlightPercentiles(zsd.toLocalDate(), 1, "LEGS, DST");
			PercentileStatsEntry dst = stdao.getFlightPercentiles(zsd.toLocalDate(), 1, "DST, LEGS");
			
			// Calculate the new levels
			Map<String, EliteLevel> newLevels = new HashMap<String, EliteLevel>();
			for (EliteLevel oldLevel : cyLevels) {
				EliteLevel lvl = new EliteLevel(oldLevel.getYear() + 1, oldLevel.getName(), oldLevel.getOwner());
				lvl.setColor(oldLevel.getColor());
				lvl.setBonusFactor(oldLevel.getBonusFactor());
				lvl.setVisible(oldLevel.getIsVisible());
				lvl.setStatisticsStartDate(zsd.toInstant());
				if (oldLevel.getLegs() > 0) {
					int targetPct = StringUtils.parse(ctx.getParameter("adjust-" + oldLevel.getName()), -1);
					lvl.setTargetPercentile((targetPct > 0) ? targetPct : oldLevel.getTargetPercentile());
					lvl.setLegs(EliteLevel.round(lst.getLegs(lvl.getTargetPercentile()) * factor, SystemData.getInt("econ.elite.round.leg", 5)));
					lvl.setDistance(EliteLevel.round(dst.getDistance(lvl.getTargetPercentile()) * factor, SystemData.getInt("econ.elite.round.distance", 5000)));
					lvl.setPoints(EliteLevel.round(lvl.getPoints() * factor, SystemData.getInt("econ.elite.round.points", 5000)));
				}
				
				newLevels.put(lvl.getName(), lvl);
			}
			
			// Write levels if needed
			if (isCommit) {
				ctx.startTX();
				SetElite ewdao = new SetElite(con);	
				for (EliteLevel nl : newLevels.values())
					ewdao.write(nl);
				
				ctx.commitTX();
			}
			
			// Save old and new levels
			ctx.setAttribute("isLevelSet", Boolean.TRUE, REQUEST);
			ctx.setAttribute("startDate", zsd, REQUEST);
			ctx.setAttribute("statsAdjustFactor", Double.valueOf(factor), REQUEST);
			ctx.setAttribute("oldLevels", CollectionUtils.createMap(cyLevels, EliteLevel::getName), REQUEST);
			ctx.setAttribute("newLevels", newLevels, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setType(isCommit ? ResultType.REQREDIRECT : ResultType.FORWARD);
		result.setURL("/jsp/econ/" + (isCommit ? "eliteLevelUpdate.jsp" : "eliteLevelSet.jsp"));
		result.setSuccess(true);
	}
}
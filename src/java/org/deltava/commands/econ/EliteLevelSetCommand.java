// Copyright 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to automatically calulate Elite levels for an upcoming year.
 * @author Luke
 * @version 11.5
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
		
		// Get the stats/status years and if we are in the rollover period
		ZonedDateTime now = ZonedDateTime.now();
		int statusYear = EliteScorer.getStatusYear(now.toInstant()); int statsYear = EliteScorer.getStatsYear(now.toInstant());
		boolean isRolloverPeriod = EliteScorer.isRollover();
		ctx.setAttribute("startDate", ZonedDateTime.of(isRolloverPeriod ? statusYear : statsYear, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), REQUEST);
		ctx.setAttribute("year", Integer.valueOf(statusYear + 1), REQUEST);
		ctx.setAttribute("isRollover", Boolean.valueOf(isRolloverPeriod), REQUEST);
		
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
			Duration d = Duration.between(zsd, now.minusHours(12)); // assume lag time for PIREP approval
			int daysInYear = Year.isLeap(now.getYear()) ? 365 : 366;
			factor = daysInYear * 24f / d.toHours();
		}
		  
		boolean isCommit = Boolean.parseBoolean(ctx.getParameter("isCommit")) && isRolloverPeriod;
		boolean calcPoints = Boolean.parseBoolean(ctx.getParameter("calcPoints"));
		try {
			Connection con = ctx.getConnection();
			
			// Get current year's levels
			GetElite edao = new GetElite(con);
			Collection<EliteLevel> cyLevels = edao.getLevels(statusYear);
			Collection<EliteLifetime> ltLevels = edao.getLifetimeLevels();
			
			// Get the PIREP statistics for the year
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			List<YearlyTotal> eTotals = esdao.getPilotTotals(zsd.toLocalDate());
			FlightPercentileHelper eHelper = new FlightPercentileHelper(eTotals, 1);
			PercentileStatsEntry lst = eHelper.getLegs(); PercentileStatsEntry dst = eHelper.getDistance(); PercentileStatsEntry pst = eHelper.getPoints(); 
			
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
					if (calcPoints)
						lvl.setPoints(EliteLevel.round(pst.getPoints(lvl.getTargetPercentile()) * factor, SystemData.getInt("econ.elite.round.points", 5000)));
				}
				
				newLevels.put(lvl.getName(), lvl);
			}
			
			// Convert lifetime status levels
			Collection<EliteLifetime> nlLevels = new ArrayList<EliteLifetime>();
			for (EliteLifetime oldLevel : ltLevels) {
				EliteLifetime nl = new EliteLifetime(oldLevel.getName());
				nl.setCode(oldLevel.getCode());
				nl.setDistance(oldLevel.getDistance());
				nl.setLegs(oldLevel.getLegs());
				Optional<EliteLevel> nel = newLevels.values().stream().filter(lvl -> lvl.matches(oldLevel.getLevel())).findAny();
				if (nel.isPresent()) {
					nl.setLevel(nel.get());
					nlLevels.add(nl);
				}
			}
			
			// Write levels if needed
			if (isCommit) {
				ctx.startTX();
				SetElite ewdao = new SetElite(con);	
				for (EliteLevel nl : newLevels.values())
					ewdao.write(nl);
				for (EliteLifetime nl : nlLevels)
					ewdao.write(nl);
				
				ctx.commitTX();
				ctx.setAttribute("isPersisted", Boolean.TRUE, REQUEST);
				CacheManager.invalidate("EliteLevel");
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
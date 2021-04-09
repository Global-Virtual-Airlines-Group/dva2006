// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.LocalDate;

import org.deltava.beans.econ.EliteLevel;
import org.deltava.beans.stats.PercentileStatsEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to automatically calulate Elite levels for an upcoming year.
 * @author Luke
 * @version 9.2
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
		
		int year = ctx.getID();
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Remove the levels for this year, if they are present stop
			GetElite edao = new GetElite(con);
			Collection<EliteLevel> allLevels = edao.getLevels(); 
			Optional<EliteLevel> cyLevelsFound = allLevels.stream().filter(lv -> lv.getYear() == year).findAny();
			if (!cyLevelsFound.isEmpty())
				throw securityException("Elite levels already populated for " + year);
			
			// Get previous year's levels
			Collection<EliteLevel> pyLevels = allLevels.stream().filter(lv -> lv.getYear() == (year - 1)).collect(Collectors.toList());
			
			// Get the PIREP statistics for the year
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			PercentileStatsEntry st = stdao.getFlightPercentiles(LocalDate.of(year - 2, 12, 1), 1, false);
			PercentileStatsEntry ast = stdao.getFlightPercentiles(LocalDate.of(year - 2, 12, 1), 1, true);
			
			// Write the new levels
			SetElite ewdao = new SetElite(con);
			int legRound = SystemData.getInt("econ.elite.round.leg", 5); int dstRound = SystemData.getInt("econ.elite.round.distance", 5000);
			Map<String, EliteLevel> newLevels = new HashMap<String, EliteLevel>();
			for (EliteLevel oldLevel : pyLevels) {
				EliteLevel lvl = new EliteLevel(year, oldLevel.getName());
				lvl.setColor(oldLevel.getColor());
				lvl.setBonusFactor(oldLevel.getBonusFactor());
				lvl.setTargetPercentile(oldLevel.getTargetPercentile());
				lvl.setPoints(oldLevel.getPoints());
				lvl.setVisible(oldLevel.getIsVisible());
				
				// Calculate legs and distance
				int legs = st.getLegs(oldLevel.getTargetPercentile()); int distance = ast.getDistance(oldLevel.getTargetPercentile());
				int avgDistance = Math.max(575,  Math.min(925, (distance / legs)));
				lvl.setLegs((lvl.getTargetPercentile() > 1) ? legs / legRound * legRound + legRound : 1);
				lvl.setDistance(lvl.getLegs() * avgDistance / dstRound * dstRound + dstRound);
				
				// Write the level
				ewdao.write(lvl);
			}
			
			// Save old and new levels
			ctx.setAttribute("isLevelSet", Boolean.TRUE, REQUEST);
			ctx.setAttribute("year", Integer.valueOf(year), REQUEST);
			ctx.setAttribute("oldLevels", CollectionUtils.createMap(pyLevels, EliteLevel::getName), REQUEST);
			ctx.setAttribute("newLevels", newLevels, REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/econ/eliteLevelUpdate.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to rollover Elite status levels for a new program year. 
 * @author Luke
 * @version 11.1
 * @since 11.0
 */

public class EliteRolloverCommand extends AbstractCommand {
	
	private static final Logger log = LogManager.getLogger(EliteRolloverCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int year = EliteScorer.getStatusYear(Instant.now()) + 1;
		try {
			Connection con = ctx.getConnection();

			// Get upcoming year's levels
			GetElite eldao = new GetElite(con);
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			TreeSet<EliteLevel> lvls = eldao.getLevels(year);
			if (lvls.isEmpty())
				throw notFoundException("No Elite status levels for " + year);
			
			// Load Pilots from last year
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = new HashSet<Integer>();
			TreeSet<EliteLevel> pyLevels = eldao.getLevels(year -1);
			for (EliteLevel lvl : pyLevels)
				IDs.addAll(eldao.getPilots(lvl));
				
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			List<String> msgs = new ArrayList<String>();
			
			// Get highest status from last year
			for (Pilot p : pilots.values()) {
				ctx.startTX();
				List<EliteStatus> status = eldao.getAllStatus(p.getID(), (year - 1));
				status.removeIf(es -> es.getUpgradeReason().isRollover());
				EliteStatus st = status.getLast();
				
				// Load totals for the year
				YearlyTotal lyt = esdao.getEliteTotals(p.getID(), (year - 1));
				if (!lyt.matches(st.getLevel())) {
					log.warn("{} has {}, totals are {} / {}", p.getName(), st.getLevel().getName(), Integer.valueOf(lyt.getLegs()), Integer.valueOf(lyt.getDistance()));
					continue;
				}
				
				// Calcualte new level
				EliteLevel newLevel = lvls.stream().filter(lv -> lv.matches(st.getLevel())).findFirst().orElse(lvls.first());
				UpgradeReason ur = (newLevel.compareTo(st.getLevel()) == 0) ? UpgradeReason.ROLLOVER : UpgradeReason.DOWNGRADE;
				msgs.add(String.format("Rolling over %s status for %s in %d / %s", newLevel.getName(), p.getName(), Integer.valueOf(year), ur.getDescription()));
				log.info("Rolling over {} status for {} in {} / {}", newLevel.getName(), p.getName(), Integer.valueOf(year), ur.getDescription());
				
				// Calculate rollover for next year
				YearlyTotal rt = new YearlyTotal(year, p.getID());
				rt.addLegs(Math.max(0, lyt.getLegs() - newLevel.getLegs()), Math.max(0, lyt.getDistance() - newLevel.getDistance()), 0);
				log.info("{} rollover = {} legs, {} miles", p.getName(), Integer.valueOf(rt.getLegs()), Integer.valueOf(rt.getDistance()));
				
				// Write the status
				SetElite elwdao = new SetElite(con);
				EliteStatus newStatus = new EliteStatus(p.getID(), newLevel);
				newStatus.setEffectiveOn(LocalDateTime.of(year, 2, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));
				newStatus.setUpgradeReason(ur);
				elwdao.write(newStatus);
				elwdao.rollover(rt);
				
				// Write status updates
				SetStatusUpdate updwdao = new SetStatusUpdate(con);
				Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
				upd.setDate(Instant.now());
				upd.setAuthorID(p.getID());
				upd.setDescription(String.format("Reached %s for %d ( %s )", newLevel.getName(), Integer.valueOf(year), ur.getDescription()));
				upds.add(upd);
				if (!rt.isZero()) {
					upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
					upd.setDate(Instant.now());
					upd.setAuthorID(p.getID());
					upd.setDescription(String.format("Rolled over %d legs / %d %s", Integer.valueOf(rt.getLegs()), Integer.valueOf(rt.getDistance()), SystemData.get("econ.elite.distance")));
					upds.add(upd);	
				}
				
				updwdao.write(upds);
				ctx.commitTX();
			}
			
			// Save status attributes
			ctx.setAttribute("isRollover", Boolean.TRUE, REQUEST);
			ctx.setAttribute("msgs", msgs, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteUpdate.jsp");
		result.setSuccess(true);
	}
}
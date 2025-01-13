// Copyright 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 11.5
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
		boolean isRollover = EliteScorer.isRollover();
		boolean doCommit = Boolean.parseBoolean(ctx.getParameter("isCommit")) && isRollover;
		boolean allowPointRollover = Boolean.parseBoolean(ctx.getParameter("allowPointRollover"));

		try {
			Connection con = ctx.getConnection();

			// Get previous year's levels
			GetElite eldao = new GetElite(con);
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			TreeSet<EliteLevel> lvls = eldao.getLevels(year - 1);
			if (lvls.isEmpty())
				throw notFoundException("No Elite status levels for " + (year - 1));
			
			// Load current year's levels
			TreeSet<EliteLevel> nyLevels = eldao.getLevels(year);
			if (nyLevels.isEmpty())
				throw notFoundException("No Elite status levels for " + year);
			
			// Disable point rollover
			if (!allowPointRollover)
				lvls.forEach(lv -> lv.setPoints(0));
			
			// Load Pilots from last year
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = new TreeSet<Integer>();
			for (EliteLevel lvl : lvls)
				IDs.addAll(eldao.getPilots(lvl));
			
			//IDs.clear(); IDs.addAll(Set.of(8027, 0x966bc, 0x972c7, 0x2142, 0x92862, 0x9349a, 0x1fdc));
				
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			List<String> msgs = new ArrayList<String>();
			
			// Get highest status from last year
			int rolloverCount = 0; int downgradeCount = 0;
			for (Pilot p : pilots.values()) {
				
				// Load status for past year, including rollover from year - 2
				List<EliteStatus> status = eldao.getAllStatus(p.getID(), (year - 1));
				boolean hasRollover = status.stream().anyMatch(es -> es.getUpgradeReason().isRollover());
				EliteLevel rlvl = hasRollover ? status.stream().filter(es -> es.getUpgradeReason().isRollover()).findFirst().orElse(null).getLevel() : lvls.first();
				EliteLevel lvl = status.isEmpty() ? lvls.getFirst() : status.getLast().getLevel(); // last level including rollover
				
				// Get upcoming year's status
				YearlyTotal lyt = esdao.getEliteTotals(p.getID(), (year - 1));
				EliteLevel pyLevel = lyt.matches(lvls);
				if (!pyLevel.matches(lvl) && !hasRollover)
					msgs.add(String.format("%s should be %s for %d, Rollover = %s, Actual = %s", p.getName(), pyLevel.getName(), Integer.valueOf(year), rlvl.getName(), lvl.getName()));
				
				// Check for lifetime status
				EliteLifetimeStatus els = eldao.getLifetimeStatus(p.getID(), ctx.getDB());
				boolean hasLTStatus = new EliteStatus(p.getID(), lvl).overridenBy(els);
				boolean hasBaseLevel = pyLevel.matches(lvl) && pyLevel.matches(lvls.getFirst());
				
				// Compare totals for the year - break if not rollover eligible or remaining at lowest level
				if (!p.getStatus().isActive()) {
					msgs.add(String.format("%s status = %s, no rollover", p.getName(), p.getStatus().getDescription()));
					continue;
				} else if (hasBaseLevel && !hasLTStatus) {
					msgs.add(String.format("%s remains as %s for %d", p.getName(), lvl.getName(), Integer.valueOf(year)));
					continue;
				}
				
				// Create status update
				Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
				upd.setDate(Instant.now());
				upd.setAuthorID(p.getID());
				
				// Additional log if lifetime status exceeds earned status
				if (hasLTStatus) {
					msgs.add(String.format("Lifetime %s status exceeds %s for %s in %d", els.getLifetimeStatus().getName(), lvl.getName(), p.getName(), Integer.valueOf(year)));
					log.info("Continuing {} status for {} in {}", els.getLifetimeStatus().getName(), p.getName(), Integer.valueOf(year));
					StatusUpdate upd2 = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
					upd2.setDate(Instant.now());
					upd2.setAuthorID(p.getID());
					upd2.setDescription(String.format("Lifetime %s status exceeds %s for %s in %d", els.getLifetimeStatus().getName(), lvl.getName(), p.getName(), Integer.valueOf(year)));
					upds.add(upd2);
				}
				
				// Break if retain member level
				if (hasBaseLevel) continue;
				
				// Calcualte new level
				UpgradeReason ur = pyLevel.matches(lvl) ? UpgradeReason.ROLLOVER : UpgradeReason.DOWNGRADE;
				if (ur == UpgradeReason.ROLLOVER) {
					msgs.add(String.format("Rolling over %s status for %s in %d / %s", pyLevel.getName(), p.getName(), Integer.valueOf(year), ur.getDescription()));
					log.info("Rolling over {} status for {} in {} / {}", pyLevel.getName(), p.getName(), Integer.valueOf(year), ur.getDescription());
					upd.setDescription(String.format("Rolled over %s for %d", pyLevel.getName(), Integer.valueOf(year)));
					upds.add(upd);
					rolloverCount++;
				} else {
					msgs.add(String.format("%s downgraded from %s to %s in %d / %s", p.getName(), lvl.getName(), pyLevel.getName(), Integer.valueOf(year), ur.getDescription()));
					log.info("{} downgraded from {} to {} in {} / {}", p.getName(), lvl.getName(), pyLevel.getName(), Integer.valueOf(year), ur.getDescription());
					upd.setDescription(String.format("Downgraded from %s to %s for %d", lvl.getName(), pyLevel.getName(), Integer.valueOf(year)));
					upds.add(upd);
					downgradeCount++;
				}
				
				// Calculate rollover for next year
				YearlyTotal rt = new YearlyTotal(year, p.getID());
				rt.addLegs(Math.max(0, lyt.getLegs() - pyLevel.getLegs()), Math.max(0, lyt.getDistance() - pyLevel.getDistance()), 0);
				log.info("{} rollover = {} legs, {} miles", p.getName(), Integer.valueOf(rt.getLegs()), Integer.valueOf(rt.getDistance()));
				
				// Get next year's level for rollover
				EliteLevel nyLevel = nyLevels.stream().filter(lv -> lv.matches(pyLevel)).findFirst().orElse(null);
				if (nyLevel == null) {
					msgs.add(String.format("Cannot find %d equivalent for %s for %s", Integer.valueOf(year), pyLevel, p.getName()));
					log.warn("Cannot find %d equivalent for %s for %s", Integer.valueOf(year), pyLevel, p.getName());
					continue;
				}
				
				// Start transaction
				ctx.startTX();
				SetElite elwdao = new SetElite(con);
				SetStatusUpdate updwdao = new SetStatusUpdate(con);
					
				// Write the status
				EliteStatus newStatus = new EliteStatus(p.getID(), nyLevel); // needs to be current year level here
				newStatus.setEffectiveOn(LocalDateTime.of(year, 2, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));
				newStatus.setUpgradeReason(ur);
				elwdao.write(newStatus);
				
				// Write status updates
				if (!rt.isZero()) {
					elwdao.rollover(rt);
					StatusUpdate upd2 = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
					upd2.setDate(Instant.now());
					upd2.setAuthorID(p.getID());
					upd2.setDescription(String.format("Rolled over %d legs / %d %s", Integer.valueOf(rt.getLegs()), Integer.valueOf(rt.getDistance()), SystemData.get("econ.elite.distance")));
					upds.add(upd2);	
				}
				
				updwdao.write(upds);
				if (doCommit)
					ctx.commitTX();
				else
					ctx.rollbackTX();
			}
			
			// Save status attributes
			ctx.setAttribute("isRollover", Boolean.TRUE, REQUEST);
			ctx.setAttribute("year", Integer.valueOf(year), REQUEST);
			ctx.setAttribute("isPersisted", Boolean.valueOf(doCommit), REQUEST);
			ctx.setAttribute("isRolloverPeriod", Boolean.valueOf(isRollover), REQUEST);
			ctx.setAttribute("downgrades", Integer.valueOf(downgradeCount), REQUEST);
			ctx.setAttribute("rollovers", Integer.valueOf(rolloverCount), REQUEST);
			ctx.setAttribute("msgs", msgs, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/" + (doCommit ? "eliteLevelUpdate.jsp" : "eliteRollover.jsp"));
		result.setType(doCommit ? ResultType.REQREDIRECT : ResultType.FORWARD);
		result.setSuccess(true);
	}
}
// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to rollover Elite status levels for a new program year. 
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class EliteRolloverCommand extends AbstractCommand {

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
			SetElite elwdao = new SetElite(con);
			SetStatusUpdate updwdao = new SetStatusUpdate(con);
			for (Pilot p : pilots.values()) {
				ctx.startTX();
				List<EliteStatus> status = eldao.getAllStatus(p.getID(), (year - 1));
				status.removeIf(es -> es.getUpgradeReason().isRollover());
				EliteStatus st = status.get(status.size() - 1);
				
				// Calcualte new level
				EliteLevel newLevel = lvls.stream().filter(lv -> lv.matches(st.getLevel())).findFirst().orElse(lvls.first());
				UpgradeReason ur = (newLevel.compareTo(st.getLevel()) == 0) ? UpgradeReason.ROLLOVER : UpgradeReason.DOWNGRADE;
				msgs.add(String.format("Rolling over %s status for %s in %d / %s", newLevel.getName(), p.getName(), Integer.valueOf(year), ur.getDescription()));
				
				// Write the status
				EliteStatus newStatus = new EliteStatus(p.getID(), newLevel);
				newStatus.setEffectiveOn(LocalDateTime.of(year, 2, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));
				newStatus.setUpgradeReason(ur);
				elwdao.write(newStatus);
				
				// Write a status update
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
				upd.setDate(Instant.now());
				upd.setAuthorID(p.getID());
				upd.setDescription(String.format("Reached %s for %d ( %s )", newLevel.getName(), Integer.valueOf(year), ur.getDescription()));
				updwdao.write(upd, ctx.getDB());
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
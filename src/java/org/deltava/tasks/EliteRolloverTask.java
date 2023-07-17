// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

/**
 * A Scheduled Task to roll over Elite status for Pilots. 
 * @author Luke
 * @version 11.0
 * @since 9.2
 */

public class EliteRolloverTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public EliteRolloverTask() {
		super("Elite Rollover", EliteRolloverTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Check if we've run this year
		Instant lr = ctx.getLastRun(); final int yr = EliteScorer.getStatusYear(Instant.now());
		if ((lr != null) && (EliteScorer.getStatusYear(lr) == yr))
			return;
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilots
			GetPilot pdao = new GetPilot(con);
			GetElite eldao = new GetElite(con);
			SetElite elwdao = new SetElite(con);
			SetStatusUpdate updwdao = new SetStatusUpdate(con); 
			Collection<Integer> IDs = eldao.getAllPilots(yr - 1);
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			
			// Get this year's levels
			TreeSet<EliteLevel> lvls = eldao.getLevels(yr);
			
			// Get highest status from last year
			for (Pilot p : pilots.values()) {
				ctx.startTX();
				List<EliteStatus> status = eldao.getAllStatus(p.getID(), (yr - 1));
				status.removeIf(es -> (es.getUpgradeReason() == UpgradeReason.ROLLOVER || es.getUpgradeReason() == UpgradeReason.DOWNGRADE));
				EliteStatus st = status.get(status.size() - 1);
				
				// Calcualte new level
				EliteLevel newLevel = lvls.stream().filter(lv -> lv.matches(st.getLevel())).findFirst().orElse(lvls.first());
				UpgradeReason ur = (newLevel.compareTo(st.getLevel()) == 0) ? UpgradeReason.ROLLOVER : UpgradeReason.DOWNGRADE;
				log.info("Rolling over " + newLevel.getName() + " status for " + p.getName() + " in " + yr + " / " + ur.getDescription());
				
				// Write the status
				EliteStatus newStatus = new EliteStatus(p.getID(), newLevel);
				newStatus.setEffectiveOn(LocalDateTime.of(yr, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));
				newStatus.setUpgradeReason(ur);
				elwdao.write(newStatus);
				
				// Write a status update
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
				upd.setDate(Instant.now());
				upd.setAuthorID(p.getID());
				upd.setDescription(String.format("Reached %s for %d / ( %s )", newLevel.getName(), Integer.valueOf(yr), ur.getDescription()));
				updwdao.write(upd, ctx.getDB());
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
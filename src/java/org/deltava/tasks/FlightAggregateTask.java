// Copyright 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.Connection;
import java.util.Collection;

import org.apache.logging.log4j.Level;

import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.TaskTimer;

/**
 * A Scheduled Task to aggregate Flight statistics. 
 * @author Luke
 * @version 12.1
 * @since 11.1
 */

public class FlightAggregateTask extends Task {

	/**
	 * Creates the Task.
	 */
	public FlightAggregateTask() {
		super("Flight Statistics Aggregation", FlightAggregateTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetFlightReports frdao = new GetFlightReports(con);
			GetFlightReportQueue qdao = new GetFlightReportQueue(con);
			SetFlightReportQueue qwdao = new SetFlightReportQueue(con);
			SetAggregateStatistics stwdao = new SetAggregateStatistics(con);
			
			// Get the queue
			Collection<ApprovalStatus> flights = qdao.getPostApprovalQueue();
			flights.removeIf(ap -> !ap.isPending(ApprovalOperation.STATS));
			
			// Process each flight
			for (ApprovalStatus ap : flights) {
				FlightReport fr = frdao.get(ap.getID(), ctx.getDB());
				if (fr == null) {
					log.warn("Missing Flight Report - {}", Integer.valueOf(ap.getID()));
					continue;
				}
				
				TaskTimer tt = new TaskTimer();
				
				// Update statistics
				ctx.startTX();
				stwdao.update(fr);
				qwdao.complete(ap.getID(), ApprovalOperation.STATS);
				ctx.commitTX();
				
				long ms = tt.stop();
				log.log((ms > 4500) ? Level.WARN : Level.INFO, "Aggregates for Flight Report {} completed in {}ms", Integer.valueOf(ap.getID()), Long.valueOf(ms));
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			logError("Error aggregating flights", de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
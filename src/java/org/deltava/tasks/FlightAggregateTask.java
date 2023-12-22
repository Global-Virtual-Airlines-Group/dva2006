// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.Connection;
import java.util.Collection;

import org.apache.logging.log4j.Level;

import org.deltava.beans.flight.FlightReport;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.TaskTimer;

/**
 * A Scheduled Task to aggregate Flight statistics. 
 * @author Luke
 * @version 11.1
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
			
			// Load the queue
			GetFlightReports frdao = new GetFlightReports(con);
			GetAggregateStatistics stdao = new GetAggregateStatistics(con);
			Collection<Integer> IDs = stdao.getAggregateQueue();
			
			// Process each flight
			for (Integer id : IDs) {
				ctx.startTX();
				SetAggregateStatistics stwdao = new SetAggregateStatistics(con);
				
				// Get the flight
				TaskTimer tt = new TaskTimer();
				FlightReport fr = frdao.get(id.intValue(), ctx.getDB());
				if (fr != null)
					stwdao.update(fr);
				else
					log.warn("Missing Flight Report - {}", id);
				
				stwdao.deleteQueueEntry(id.intValue());
				ctx.commitTX();
				
				long ms = tt.stop();
				log.log((ms > 4500) ? Level.WARN : Level.INFO, "Aggregates for Flight Report {} completed in {}ms", id, Long.valueOf(ms));
			}
			
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
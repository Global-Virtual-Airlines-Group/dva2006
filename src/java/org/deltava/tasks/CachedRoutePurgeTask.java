// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to purge cached FlightAware routes from the database.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class CachedRoutePurgeTask extends Task {

	/**
	 * Initializes the scheduled Task.
	 */
	public CachedRoutePurgeTask() {
		super("Cached Route Purge", CachedRoutePurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {

		// Get the age to purge
		int maxAge = SystemData.getInt("schedule.flightaware.max_age", 365);
		log.info("Executing");
		try {
			SetCachedRoutes rcwdao = new SetCachedRoutes(ctx.getConnection());
			rcwdao.purge(maxAge);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}
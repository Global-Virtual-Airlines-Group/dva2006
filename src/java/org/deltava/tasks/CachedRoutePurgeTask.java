// Copyright 2009, 2010, 2011, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to purge cached FlightAware routes from the database.
 * @author Luke
 * @version 11.1
 * @since 3.6
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
		log.info("Executing");
		
		int maxAge = SystemData.getInt("schedule.flightaware.max_age", 365);
		try {
			SetCachedRoutes rcwdao = new SetCachedRoutes(ctx.getConnection());
			log.warn("Purged {} cached routes {} days of age or older", Integer.valueOf(rcwdao.purge(maxAge)), Integer.valueOf(maxAge));
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}
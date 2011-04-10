// Copyright 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to purge cached FlightAware routes from the database.
 * @author Luke
 * @version 3.6
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
			log.warn("Purged " + rcwdao.purge(maxAge) + " cached routes " + maxAge + " days of age or older");
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}
// Copyright 2005, 2006, 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge SELCAL code reservations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SELCALPurgeTask extends Task {

	/**
	 * Initailizes the task.
	 */
	public SELCALPurgeTask() {
		super("SELCAL Purge", SELCALPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		log.info("Starting");
		int purgeInterval = SystemData.getInt("users.selcal.inactive", 14);
		
		try {
			SetSELCAL dao = new SetSELCAL(ctx.getConnection());
			log.info("Freed up " + dao.free(purgeInterval) + " reserved SELCAL codes");
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}
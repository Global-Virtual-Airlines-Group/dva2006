// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge the Web Site Command log. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLogPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public CommandLogPurgeTask() {
		super("Command Log Purge", CommandLogPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {
		
		// Get the purge interval
		int purgeDays = SystemData.getInt("log.purge.cmds", 10);
		log.info("Executing");
		
		try {
			SetSystemData dao = new SetSystemData(ctx.getConnection());
			dao.purge("COMMANDS", "CMDDATE", purgeDays);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}
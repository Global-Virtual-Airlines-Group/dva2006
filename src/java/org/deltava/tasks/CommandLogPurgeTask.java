// Copyright 2005, 2006, 2007, 2016, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge the Web Site Command log. 
 * @author Luke
 * @version 11.1
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
	@Override
	protected void execute(TaskContext ctx) {
		
		// Get the purge interval
		int purgeDays = SystemData.getInt("log.purge.cmds", 10);
		log.info("Executing");
		
		try {
			SetSystemData dao = new SetSystemData(ctx.getConnection());
			dao.purgeCommands(purgeDays);
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}
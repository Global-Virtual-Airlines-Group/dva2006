// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.taskman.DatabaseTask;

import org.deltava.dao.SetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge the Web Site Command log. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLogPurgeTask extends DatabaseTask {

	/**
	 * Initializes the Task.
	 */
	public CommandLogPurgeTask() {
		super("Command Log Purge", CommandLogPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {
		
		// Get the purge interval
		int purgeDays = SystemData.getInt("log.purge.cmds", 10);
		log.info("Executing");
		
		try {
			SetSystemData dao = new SetSystemData(getConnection());
			dao.purge("COMMANDS", "CMDDATE", purgeDays);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			release();
		}
		
		log.info("Completed");
	}
}
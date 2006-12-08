// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.taskman.DatabaseTask;

import org.deltava.dao.SetSELCAL;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge SELCAL code reservations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SELCALPurgeTask extends DatabaseTask {

	/**
	 * Initailizes the task.
	 */
	public SELCALPurgeTask() {
		super("SELCAL Purge", SELCALPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {
		log.info("Starting");
		int purgeInterval = SystemData.getInt("users.selcal.inactive", 14);
		
		try {
			SetSELCAL dao = new SetSELCAL(getConnection());
			log.info("Freed up " + dao.free(purgeInterval) + " reserved SELCAL codes");
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			release();
		}

		log.info("Completed");
	}
}
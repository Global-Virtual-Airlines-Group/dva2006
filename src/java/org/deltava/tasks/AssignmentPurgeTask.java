// Copyright 2005, 2006, 2007, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.Pilot;
import org.deltava.beans.assign.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Schedule Task to automatically release Flight Assignments.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class AssignmentPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public AssignmentPurgeTask() {
		super("Assignment Purge", AssignmentPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {

		// Get the inactivity cutoff time
		int inactiveDays = SystemData.getInt("users.pirep.assign_purge", 14);
		Instant cld = Instant.now().minusSeconds(inactiveDays * 86400);
		log.info("Executing");

		try {
			Connection con = ctx.getConnection();

			// Get the assignments
			GetAssignment rdao = new GetAssignment(con);
			Collection<AssignmentInfo> assignments = rdao.getByStatus(AssignmentInfo.RESERVED);

			// Check the open assignments
			GetPilot pdao = new GetPilot(con);
			SetAssignment wdao = new SetAssignment(con);
			for (Iterator<AssignmentInfo> i = assignments.iterator(); i.hasNext();) {
				AssignmentInfo a = i.next();
				if (cld.isAfter(a.getAssignDate())) {
					Pilot usr = pdao.get(a.getPilotID());

					// If the assignment is repeatable, then release it - otherwise delete it
					if (a.isRepeating()) {
						log.warn("Releasing Assignment " + a.getID() + " reserved by " + usr.getName());
						wdao.reset(a);
					} else {
						log.warn("Deleting Assignment " + a.getID() + " reserved by " + usr.getName());
						wdao.delete(a);
					}
				} else if (log.isDebugEnabled())
					log.debug("Skipping Assignment " + a.getID() + ", assigned on " + a.getAssignDate());
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}
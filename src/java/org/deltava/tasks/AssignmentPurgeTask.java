// Copyright 2005, 2006, 2007, 2010, 2016, 2017, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 12.0
 * @since 1.0
 */

public class AssignmentPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public AssignmentPurgeTask() {
		super("Assignment Purge", AssignmentPurgeTask.class);
	}

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
			Collection<AssignmentInfo> assignments = rdao.getByStatus(AssignmentStatus.RESERVED);

			// Check the open assignments
			GetPilot pdao = new GetPilot(con);
			SetAssignment wdao = new SetAssignment(con);
			for (AssignmentInfo a :  assignments) {
				if (cld.isAfter(a.getAssignDate())) {
					Pilot usr = pdao.get(a.getPilotID());
					if (usr == null) {
						log.warn("Unknown Pilot ID - {}", Integer.valueOf(a.getPilotID()));
						continue;
					}

					// If the assignment is repeatable, then release it - otherwise delete it
					if (a.isRepeating()) {
						log.info("Releasing Assignment {} reserved by {}", Integer.valueOf(a.getID()), usr.getName());
						wdao.reset(a);
					} else {
						log.info("Deleting Assignment {} reserved by {}", Integer.valueOf(a.getID()), usr.getName());
						wdao.delete(a);
					}
				} else if (log.isDebugEnabled())
					log.debug("Skipping Assignment {} assigned on {}", Integer.valueOf(a.getID()), a.getAssignDate());
			}
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}
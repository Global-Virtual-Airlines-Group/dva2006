// Copyright 2011, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.hr.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

/**
 * A Scheduled Task to automatically close Job Postings.
 * @author Luke
 * @version 10.2
 * @since 3.6
 */

public class JobCloseTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public JobCloseTask() {
		super("Close Job Postings", JobCloseTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get all jobs
			GetJobs jdao = new GetJobs(con);
			List<JobPosting> openJobs = jdao.getOpen();
			Instant now = Instant.now();
			
			// Close them if required
			SetJobs jwdao = new SetJobs(con);
			for (JobPosting jp : openJobs) {
				if (jp.getClosesOn().isBefore(now)) {
					log.info("Closing Job " + jp.getTitle());
					jp.setStatus(JobStatus.CLOSED);
					jwdao.write(jp);
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
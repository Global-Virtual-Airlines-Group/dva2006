// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.hr.JobPosting;

import org.deltava.dao.*;
import org.deltava.taskman.*;

/**
 * A Scheduled Task to automatically close Job Postings.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class JobCloseTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public JobCloseTask() {
		super("Close Job Postings", JobCloseTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get all jobs
			GetJobs jdao = new GetJobs(con);
			List<JobPosting> openJobs = jdao.getOpen();
			
			// Close them if required
			SetJobs jwdao = new SetJobs(con);
			for (Iterator<JobPosting> i = openJobs.iterator(); i.hasNext(); ) {
				JobPosting jp = i.next();
				log.info("Closing Job " + jp.getTitle());
				jp.setStatus(JobPosting.CLOSED);
				jwdao.write(jp);
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
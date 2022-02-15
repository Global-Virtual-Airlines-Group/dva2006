// Copyright 2011, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;

import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 * A Web Site Command to reset a closed Job Posting.
 * @author Luke
 * @version 10.2
 * @since 3.6
 */

public class JobResetCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the Job Posting
			GetJobs dao = new GetJobs(con);
			JobPosting jp = dao.get(ctx.getID());
			if (jp == null)
				throw notFoundException("Unknown Job Posting - " + ctx.getID());
			
			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanReset())
				throw securityException("Cannot reset short list for Job Posting " + jp.getID());
			
			// Start transaction
			ctx.startTX();
			
			// Update the job and its applicants
			SetJobs jwdao = new SetJobs(con);
			jp.setStatus(JobStatus.CLOSED);
			jwdao.write(jp);
			for (Application a : jp.getApplications()) {
				if (a.getStatus() == ApplicantStatus.SHORTLIST) {
					a.setStatus(ApplicantStatus.PENDING);
					jwdao.write(a);
				}
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the command
		CommandResult result = ctx.getResult();
		result.setURL("job", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}
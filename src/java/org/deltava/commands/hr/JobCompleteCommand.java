// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.hr.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 * A Web Site Command to mark a Job Posting hire process as complete.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class JobCompleteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Job profile
			GetJobs dao = new GetJobs(con);
			JobPosting jp = dao.get(ctx.getID());
			if (jp == null)
				throw notFoundException("Unknown Job Posting - " + ctx.getID());

			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanComplete())
				throw securityException("Cannot mark Job Posting " + jp.getID() + " complete");
			
			// Start transaction
			ctx.startTX();
			
			// Update status
			SetJobs jwdao = new SetJobs(con);
			jp.setStatus(JobPosting.COMPLETE);
			jwdao.write(jp);
			
			// Write comment
			Comment c = new Comment(jp.getID(), ctx.getUser().getID());
			c.setCreatedOn(Instant.now());
			c.setBody("Completed hiring process");
			jwdao.write(c);
			
			// Commit
			ctx.commitTX();

			// Save status attributes
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("isCompleted", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/jobPostUpdate.jsp");
		result.setSuccess(true);
	}
}
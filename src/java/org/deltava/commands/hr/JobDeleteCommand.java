// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;

import org.deltava.beans.hr.JobPosting;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 *A Web Site Command to delete a Job posting. 
 * @author Luke
 * @version 3.7
 * @since 3.7
 */

public class JobDeleteCommand extends AbstractCommand {

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
			if (!access.getCanDelete())
				throw securityException("Cannot delete Job Posting " + jp.getID());
			
			// Delete the Job
			SetJobs jwdao = new SetJobs(con);
			jwdao.deleteJob(jp.getID());
			
			// Set status variables
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
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
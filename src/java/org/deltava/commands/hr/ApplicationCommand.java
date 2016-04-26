// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 * A Web Site Command to apply to a Job Posting.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class ApplicationCommand extends AbstractCommand {

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
			if (!access.getCanApply())
				throw securityException("Cannot apply to Job Posting " + jp.getID());
			
			// Create the application
			Application a = new Application(jp.getID(), ctx.getUser().getID());
			a.setCreatedOn(Instant.now());
			a.setBody(ctx.getParameter("body"));
			
			// Check if we're saving a profile
			boolean saveProfile = Boolean.valueOf(ctx.getParameter("saveProfile")).booleanValue();
			
			// Start transaction
			ctx.startTX();
			
			// Write the application
			SetJobs jwdao = new SetJobs(con);
			jwdao.write(a);
			if (saveProfile) {
				Profile p = new Profile(a);
				p.setAutoReuse(Boolean.valueOf(ctx.getParameter("autoSubmit")).booleanValue());
				jwdao.write(p);
			}
			
			// Commit
			ctx.commitTX();
			
			// Set status attributes
			ctx.setAttribute("jobPost", jp, REQUEST);
			ctx.setAttribute("isApply", Boolean.TRUE, REQUEST);
			ctx.setAttribute("saveProfile", Boolean.valueOf(saveProfile), REQUEST);
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
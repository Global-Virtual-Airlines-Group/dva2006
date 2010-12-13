// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;
import java.util.Date;

import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 * A Web Site Command to create a Job Posting comment.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class JobCommentCommand extends AbstractCommand {

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
			if (!access.getCanComment())
				throw securityException("Cannot comment on Job Posting " + jp.getID());
			
			// Create the comment
			Comment c = new Comment(jp.getID(), ctx.getUser().getID());
			c.setCreatedOn(new Date());
			c.setBody(ctx.getParameter("body"));
			
			// Write the comment
			SetJobs jwdao = new SetJobs(con);
			jwdao.write(c);
		} catch(DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the Job Posting
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("job", null, ctx.getID());
		result.setSuccess(true);
	}
}
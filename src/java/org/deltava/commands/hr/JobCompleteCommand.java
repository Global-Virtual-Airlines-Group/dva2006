// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.hr.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to select short-listed applicants for a Job Posting. 
 * @author Luke
 * @version 3.4
 * @since 3.4
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
			
			// Load the Job Posting
			GetJobs dao = new GetJobs(con);
			JobPosting jp = dao.get(ctx.getID());
			if (jp == null)
				throw notFoundException("Unknown Job Posting - " + ctx.getID());
			
			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanSelect())
				throw securityException("Cannot select shortlisted Applicant for Job Posting " + jp.getID());
			
			// Start transaction
			ctx.startTX();
			
			// Update status
			SetJobs jwdao = new SetJobs(con);
			jp.setStatus(JobPosting.COMPLETE);
			jwdao.write(jp);
			
			// Select applicants
			Collection<String> appIDs = ctx.getParameters("sl");
			Collection<Application> selected = new ArrayList<Application>();
			for (Iterator<Application> i = jp.getApplications().iterator(); i.hasNext(); ) {
				Application a = i.next();
				boolean isSelected = appIDs.contains(StringUtils.formatHex(a.getAuthorID()));
				if (a.getShortlisted() && isSelected) {
					a.setStatus(Application.APPROVED);
					selected.add(a);
				} else if (a.getApproved() && !isSelected)
					a.setStatus(Application.SHORTLIST);
				
				jwdao.write(a);
			}
			
			// Commit
			ctx.commitTX();

			// Save status attributes
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("selected", selected, REQUEST);
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
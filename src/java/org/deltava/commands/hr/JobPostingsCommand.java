// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;

import org.deltava.beans.hr.JobPosting;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

/**
 * A Web Site Command to display open Job postings. 
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class JobPostingsCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        ViewContext<JobPosting> vc = initView(ctx, JobPosting.class);
        try {
        	GetJobs dao = new GetJobs(ctx.getConnection());
        	dao.setQueryStart(vc.getStart());
        	dao.setQueryMax(Math.round(vc.getCount() * 1.25f));
        	
        	// Load the jobs - removing ones we cannot see
        	List<JobPosting> results = ctx.isUserInRole("HR") ? dao.getAll() : dao.getActive();
        	for (Iterator<JobPosting> i = results.iterator(); i.hasNext(); ) {
        		JobPosting jp = i.next();
        		try {
        			JobPostingAccessControl ac = new JobPostingAccessControl(ctx, jp);
        			ac.validate();
        		} catch (AccessControlException ace) {
        			i.remove();
        		}
        	}
        	
        	vc.setResults(results.size() > vc.getCount() ? results.subList(0, vc.getCount()) : results);
        } catch (DAOException de) {
        	throw new CommandException(de);
        } finally {
        	ctx.release();
        }
        
        // Save access rights
        JobPostingAccessControl ac = new JobPostingAccessControl(ctx, null);
        ac.validate();
        ctx.setAttribute("access", ac, REQUEST);
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/hr/jobPostings.jsp");
        result.setSuccess(true);
	}
}
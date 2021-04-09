// Copyright 2011, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.hr.JobPosting;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.JobPostingAccessControl;

/**
 * A Web Site Command to clone a Job posting.
 * @author Luke
 * @version 10.0
 * @since 3.7
 */

public class JobCloneCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the Job Posting to clone
			GetJobs dao = new GetJobs(con);
			JobPosting jp = dao.get(ctx.getID());
			if (jp == null)
				throw notFoundException("Unknown Job Posting - " + ctx.getID());
			
			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanComment())
				throw securityException("Cannot clone Job Posting " + jp.getID());
			
			// Check create access
			access = new JobPostingAccessControl(ctx, null);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot clone Job Posting " + jp.getID());
			
			// Create the new job posting
			JobPosting newjob = new JobPosting(jp.getTitle());
			newjob.setMinAge(jp.getMinAge());
			newjob.setMinLegs(jp.getMinLegs());
			newjob.setStaffOnly(true);
			newjob.setCreatedOn(Instant.now());
			newjob.setClosesOn(newjob.getCreatedOn().plusSeconds(14 * 86400));
			newjob.setHireManagerID(jp.getHireManagerID());
			newjob.setSummary(jp.getSummary());
			newjob.setDescription(jp.getDescription());

			// Load hiring manager choices
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Collection<Pilot> hMgrs = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			hMgrs.addAll(pdao.getByRole("HR", ctx.getDB()));
			hMgrs.addAll(pdao.getByRole("HireMgr", ctx.getDB()));
			hMgrs.addAll(pdao.getPilotsByRank(Rank.CP));
			ctx.setAttribute("hireMgrs", hMgrs, REQUEST);
			
			// Save in request
			ctx.setAttribute("job", newjob, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("statuses", Arrays.asList(JobPosting.STATUS_NAMES), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/jobPostingEdit.jsp");
		result.setSuccess(true);
	}
}
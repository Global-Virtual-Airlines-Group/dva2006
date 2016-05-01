// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to list all Job Applications.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class ApplicationListCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext<Application> vc = initView(ctx, Application.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get all of the jobs
			GetJobs dao = new GetJobs(con);
			Collection<JobPosting> allJobs = dao.getAll();
			ctx.setAttribute("jobs", CollectionUtils.createMap(dao.getAll(), "ID"), REQUEST);
			
			// Load hiring managers
			Collection<Integer> IDs = allJobs.stream().map(JobPosting::getHireManagerID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("hireMgrs", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Get the applications
        	dao.setQueryStart(vc.getStart());
        	dao.setQueryMax(vc.getCount());
        	vc.setResults(dao.getApplications());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/jobApps.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2006, 2007, 2008, 2009, 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.help.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.RoleUtils;

/**
 * A Web Site Command to display the Help Desk.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class HelpDeskCommand extends AbstractViewCommand {
	
	private static final List<String> ADMIN_ROLES = List.of("HR", "PIREP", "Examination", "Instrutor", "AcademyAdmin", "HelpDesk");

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		int myID = ctx.getUser().getID();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and my issue list
			GetHelp idao = new GetHelp(con);
			Collection<Issue> myIssues = idao.getByPilot(myID, myID, false, true);
			myIssues = myIssues.stream().filter(i -> (i.getStatus() != Issue.CLOSED)).collect(Collectors.toList());
			
			// Add Active issues
			Collection<Issue> allIssues = new HashSet<Issue>(myIssues);
			if (RoleUtils.hasAccess(ctx.getRoles(), ADMIN_ROLES)) {
				ViewContext<Issue> vctx = initView(ctx, Issue.class);
				idao.setQueryStart(vctx.getStart());
				idao.setQueryMax(vctx.getCount());
				Collection<Issue> activeIssues = idao.getActive();
				allIssues.addAll(activeIssues);
				vctx.setResults(activeIssues);
			}
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Issue is : allIssues) {
				IDs.add(Integer.valueOf(is.getAuthorID()));
				IDs.add(Integer.valueOf(is.getAssignedTo()));
				IDs.add(Integer.valueOf(is.getLastCommentAuthorID()));
			}

			// Calculate access rights
			HelpDeskAccessControl access = new HelpDeskAccessControl(ctx, null);
			access.validate();
			
			// Save results and access controller
			ctx.setAttribute("myIssues", myIssues, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			
			// Load Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/helpDesk.jsp");
		result.setSuccess(true);
	}
}
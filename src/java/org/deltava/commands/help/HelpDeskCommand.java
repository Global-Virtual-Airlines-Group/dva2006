// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.Issue;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.RoleUtils;

/**
 * A Web Site Command to display the Help Desk.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpDeskCommand extends AbstractCommand {
	
	private static final List<String> ADMIN_ROLES = Arrays.asList(new String[] {"HR", "PIREP", "Examination", "Instrutor", "AcademyAdmin"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and my issue list
			GetHelp idao = new GetHelp(con);
			Collection<Issue> myIssues = idao.getByPilot(ctx.getUser().getID(), false); 
			
			// Add Active issues
			Collection<Issue> allIssues = new HashSet<Issue>(myIssues);
			if (RoleUtils.hasAccess(ctx.getRoles(), ADMIN_ROLES)) {
				idao.setQueryMax(20);
				Collection<Issue> activeIssues = idao.getActive();
				allIssues.addAll(activeIssues);
				ctx.setAttribute("activeIssues", activeIssues, REQUEST);
			}
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Issue> i = allIssues.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				IDs.add(new Integer(is.getAuthorID()));
				IDs.add(new Integer(is.getAssignedTo()));
				IDs.add(new Integer(is.getLastCommentAuthorID()));
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
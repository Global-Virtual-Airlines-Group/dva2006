// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to search the Help Desk.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class HelpDeskSearchCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get search options
		int maxResults = StringUtils.parse(ctx.getParameter("maxResults"), 20);
		boolean searchComments = Boolean.valueOf(ctx.getParameter("doComments")).booleanValue();

		// Get command result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("searchStr") == null) {
			result.setURL("/jsp/help/issueSearch.jsp");
			result.setSuccess(true);
			return;
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Issues
			GetHelp dao = new GetHelp(con);
			dao.setQueryMax(Math.round(maxResults * 1.35f));
			Collection<Issue> issues = dao.search(ctx.getParameter("searchStr"), searchComments);
			
			// Check our access and get author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Issue> i = issues.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				HelpDeskAccessControl access = new HelpDeskAccessControl(ctx, is);
				try {
					access.validate();
					IDs.add(new Integer(is.getAuthorID()));
					IDs.add(new Integer(is.getAssignedTo()));
					IDs.add(new Integer(is.getLastCommentAuthorID()));
				} catch (AccessControlException ac) {
					i.remove();
				}
			}
			
			// Load Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			ctx.setAttribute("results", issues, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attributes
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		ctx.setAttribute("maxResults", Integer.valueOf(maxResults), REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/help/issueSearch.jsp");
		result.setSuccess(true);
	}
}
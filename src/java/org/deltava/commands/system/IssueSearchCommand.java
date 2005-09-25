// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

/**
 * A Web Site Command to search Issues and comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueSearchCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get max results
		int maxResults = 20;
		try {
			maxResults = Integer.parseInt(ctx.getParameter("maxResults"));
			ctx.setAttribute("maxResults", new Integer(maxResults), REQUEST);
		} catch (Exception e) { }

		// Get command result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("searchStr") == null) {
			result.setURL("/jsp/issue/issueSearch.jsp");
			result.setSuccess(true);
			return;
		}
		
		boolean searchComments = Boolean.valueOf(ctx.getParameter("doComments")).booleanValue();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and search
			GetIssue dao = new GetIssue(con);
			dao.setQueryMax(maxResults);
			ctx.setAttribute("results", dao.search(ctx.getParameter("searchStr"), searchComments), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/issue/issueSearch.jsp");
		result.setSuccess(true);
	}
}
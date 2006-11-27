// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.beans.system.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;

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
		
		// Get search options
		int maxResults = StringUtils.parse(ctx.getParameter("maxResults"), 20);
		boolean searchComments = Boolean.valueOf(ctx.getParameter("doComments")).booleanValue();
		int status = StringUtils.arrayIndexOf(Issue.STATUS, ctx.getParameter("status"));
		int area = StringUtils.arrayIndexOf(Issue.AREA, ctx.getParameter("area"));
		
		// Save combo options
		ctx.setAttribute("statusOpts", ComboUtils.fromArray(Issue.STATUS), REQUEST);
		ctx.setAttribute("areaOpts", ComboUtils.fromArray(Issue.AREA), REQUEST);

		// Get command result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("searchStr") == null) {
			result.setURL("/jsp/issue/issueSearch.jsp");
			result.setSuccess(true);
			return;
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and search
			GetIssue dao = new GetIssue(con);
			dao.setQueryMax(maxResults);
			ctx.setAttribute("results", dao.search(ctx.getParameter("searchStr"), status, area, searchComments), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attributes
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		ctx.setAttribute("maxResults", new Integer(maxResults), REQUEST);
		
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/issue/issueSearch.jsp");
		result.setSuccess(true);
	}
}
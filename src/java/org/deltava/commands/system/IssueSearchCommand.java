// Copyright 2005, 2006, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search Issues and comments.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IssueSearchCommand extends AbstractCommand {

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
		IssueStatus status = EnumUtils.parse(IssueStatus.class, ctx.getParameter("status"), null);
		IssueArea area = EnumUtils.parse(IssueArea.class, ctx.getParameter("area"), null);
		
		// Get command result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("searchStr") == null) {
			result.setURL("/jsp/issue/issueSearch.jsp");
			result.setSuccess(true);
			return;
		}
		
		String aCode = ctx.isUserInRole("Developer") ? null : SystemData.get("airline.code");
		try {
			GetIssue dao = new GetIssue(ctx.getConnection());
			dao.setQueryMax(maxResults);
			ctx.setAttribute("results", dao.search(ctx.getParameter("searchStr"), status, area, aCode, searchComments), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attributes
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		ctx.setAttribute("maxResults", Integer.valueOf(maxResults), REQUEST);
		
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/issue/issueSearch.jsp");
		result.setSuccess(true);
	}
}